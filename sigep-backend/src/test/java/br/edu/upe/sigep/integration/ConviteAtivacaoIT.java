package br.edu.upe.sigep.integration;

import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.exception.ConviteInvalidoException;
import br.edu.upe.sigep.repository.ConviteAtivacaoRepository;
import br.edu.upe.sigep.service.CadastroFuncionarioResultado;
import br.edu.upe.sigep.service.ConviteAtivacaoService;
import br.edu.upe.sigep.service.TokenService;
import br.edu.upe.sigep.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConviteAtivacaoIT extends IntegracaoUsuarios {

    private static final String SENHA = "senha-integracao-vencedora";
    private static final String OUTRA_SENHA = "senha-integracao-alternativa";
    private static final int TIMEOUT_SEGUNDOS = 10;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConviteAtivacaoService ativacao;

    @Autowired
    private ConviteAtivacaoRepository convites;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    void deveAtivarContaUmaVezSemAlterarSenhaNaSegundaTentativa() {
        var cadastro = cadastrarFuncionario();

        ativacao.ativarConta(cadastro.tokenConvite(), SENHA);
        verificarContaAtivada(cadastro);
        var usoAntes = convites.findByTokenHash(tokenService.gerarHash(cadastro.tokenConvite()))
                .orElseThrow().getUtilizadoEm();

        assertThrows(ConviteInvalidoException.class,
                () -> ativacao.ativarConta(cadastro.tokenConvite(), OUTRA_SENHA));

        verificarContaAtivada(cadastro);
        assertEquals(usoAntes, convites.findByTokenHash(tokenService.gerarHash(cadastro.tokenConvite()))
                .orElseThrow().getUtilizadoEm());
    }

    @Test
    void deveReverterAtivacaoEManterConviteDisponivelQuandoTransacaoFalha() {
        var cadastro = cadastrarFuncionario();
        String hashConvite = tokenService.gerarHash(cadastro.tokenConvite());
        IllegalStateException falha = new IllegalStateException("Falha deliberada após ativação");

        assertSame(falha, assertThrows(IllegalStateException.class, () ->
                transacoes.executeWithoutResult(status -> {
                    ativacao.ativarConta(cadastro.tokenConvite(), SENHA);
                    verificarContaAtivada(cadastro);
                    throw falha;
                })));

        transacoes.executeWithoutResult(status -> {
            var usuario = usuarios.findById(cadastro.usuario().id()).orElseThrow();
            assertFalse(usuario.isAtivo());
            assertTrue(usuario.isPrimeiroAcesso());
            assertNull(usuario.getSenhaHash());
            assertNull(convites.findByTokenHash(hashConvite).orElseThrow().getUtilizadoEm());
        });
    }

    @Test
    void deveAceitarUmaUnicaTentativaConcorrenteParaMesmoConvite() throws Exception {
        var cadastro = cadastrarFuncionario();
        var primeiraPronta = new CompletableFuture<Integer>();
        var segundaPronta = new CompletableFuture<Integer>();
        var liberarCommit = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);

        try {
            var primeira = executor.submit(() -> transacoes.executeWithoutResult(status -> {
                configurarTimeoutDeLock();
                ativacao.ativarConta(cadastro.tokenConvite(), SENHA);
                primeiraPronta.complete(pidDaTransacao());
                try {
                    if (!liberarCommit.await(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Tempo limite aguardando liberação do commit");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Transação de teste interrompida", e);
                }
            }));
            int pidPrimeira = primeiraPronta.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            var segunda = executor.submit(() -> assertThrows(ConviteInvalidoException.class,
                    () -> transacoes.executeWithoutResult(status -> {
                        configurarTimeoutDeLock();
                        segundaPronta.complete(pidDaTransacao());
                        ativacao.ativarConta(cadastro.tokenConvite(), OUTRA_SENHA);
                    })));
            int pidSegunda = segundaPronta.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            assertNotEquals(pidPrimeira, pidSegunda);
            aguardarBloqueio(pidPrimeira, pidSegunda);
            liberarCommit.countDown();

            primeira.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            segunda.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            verificarContaAtivada(cadastro);
        } finally {
            liberarCommit.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS),
                    "As transações do teste devem terminar");
        }
    }

    private CadastroFuncionarioResultado cadastrarFuncionario() {
        return usuarioService.cadastrarFuncionario(
                new UsuarioCadastroRequestDTO("Funcionário Teste", "funcionario@sigep.example"),
                criarCoordenador()
        );
    }

    private void verificarContaAtivada(CadastroFuncionarioResultado cadastro) {
        transacoes.executeWithoutResult(status -> {
            var usuario = usuarios.findById(cadastro.usuario().id()).orElseThrow();
            var convite = convites.findByTokenHash(tokenService.gerarHash(cadastro.tokenConvite())).orElseThrow();
            assertTrue(usuario.isAtivo());
            assertFalse(usuario.isPrimeiroAcesso());
            assertNotEquals(SENHA, usuario.getSenhaHash());
            assertTrue(encoder.matches(SENHA, usuario.getSenhaHash()));
            assertNotNull(convite.getUtilizadoEm());
        });
    }

    private void configurarTimeoutDeLock() {
        jdbc.execute("set local lock_timeout = '10s'");
    }

    private int pidDaTransacao() {
        return jdbc.queryForObject("select pg_backend_pid()", Integer.class);
    }

    private void aguardarBloqueio(int pidPrimeira, int pidSegunda) {
        long limite = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        do {
            if (Boolean.TRUE.equals(jdbc.queryForObject(
                    "select ? = any(pg_blocking_pids(?))", Boolean.class, pidPrimeira, pidSegunda))) {
                return;
            }
            LockSupport.parkNanos(Duration.ofMillis(20).toNanos());
        } while (System.nanoTime() < limite);
        fail("O PostgreSQL não registrou a segunda transação aguardando a primeira.");
    }
}
