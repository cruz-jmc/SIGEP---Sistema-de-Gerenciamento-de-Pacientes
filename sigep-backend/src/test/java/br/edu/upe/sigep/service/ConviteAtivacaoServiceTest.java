package br.edu.upe.sigep.service;

import br.edu.upe.sigep.entity.ConviteAtivacao;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.exception.ConviteInvalidoException;
import br.edu.upe.sigep.repository.ConviteAtivacaoRepository;
import br.edu.upe.sigep.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConviteAtivacaoServiceTest {

    private static final String TOKEN = "token-ficticio";
    private static final String SENHA = "senha-ficticia";
    private static final String HASH_TOKEN = new TokenService().gerarHash(TOKEN);

    private final ConviteAtivacaoRepository convites = mock(ConviteAtivacaoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = spy(new BCryptPasswordEncoder(4));
    private final ConviteAtivacaoService service =
            new ConviteAtivacaoService(convites, new TokenService(), usuarios, encoder);

    @Test
    void deveCriarConviteComHashVinculoEValidadeDe24Horas() {
        Usuario usuario = novoFuncionario();

        String token = service.criarConvite(usuario);

        ArgumentCaptor<ConviteAtivacao> captor = ArgumentCaptor.forClass(ConviteAtivacao.class);
        verify(convites).save(captor.capture());
        ConviteAtivacao salvo = captor.getValue();

        assertSame(usuario, salvo.getUsuario());
        assertEquals(new TokenService().gerarHash(token), salvo.getTokenHash());
        assertNotEquals(token, salvo.getTokenHash());
        assertNotNull(salvo.getCriadoEm());
        assertEquals(salvo.getCriadoEm().plusHours(24), salvo.getExpiraEm());
        assertNull(salvo.getUtilizadoEm());
    }

    @Test
    void deveRecusarConviteParaUsuarioNuloOuSemId() {
        assertThrows(IllegalArgumentException.class, () -> service.criarConvite(null));
        assertThrows(IllegalArgumentException.class, () -> service.criarConvite(new Usuario()));
        verifyNoInteractions(convites);
    }

    @Test
    void devePropagarFalhaAoPersistirNovoConvite() {
        DataIntegrityViolationException falha = new DataIntegrityViolationException("Falha no convite");
        when(convites.save(any(ConviteAtivacao.class))).thenThrow(falha);

        assertSame(falha, assertThrows(DataIntegrityViolationException.class,
                () -> service.criarConvite(novoFuncionario())));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t "})
    void deveRecusarTokenAusenteOuEmBranco(String token) {
        assertThrows(IllegalArgumentException.class, () -> service.ativarConta(token, SENHA));
        verifyNoInteractions(convites, usuarios, encoder);
    }

    @Test
    void deveRecusarTokenInexistente() {
        when(convites.buscarPorTokenHashComBloqueio(HASH_TOKEN)).thenReturn(Optional.empty());

        assertThrows(ConviteInvalidoException.class, () -> service.ativarConta(TOKEN, SENHA));
        verify(convites).buscarPorTokenHashComBloqueio(HASH_TOKEN);
        verify(convites, never()).save(any());
        verifyNoInteractions(usuarios, encoder);
    }

    @Test
    void deveRecusarConviteUtilizado() {
        ConviteAtivacao convite = conviteValido();
        convite.setUtilizadoEm(LocalDateTime.now().minusHours(1));

        verificarRejeicaoSemAlteracoes(convite);
    }

    @Test
    void deveRecusarConviteExpirado() {
        ConviteAtivacao convite = conviteValido();
        convite.setExpiraEm(LocalDateTime.now().minusHours(1));

        verificarRejeicaoSemAlteracoes(convite);
    }

    @Test
    void deveRecusarConviteDeUsuarioAtivo() {
        ConviteAtivacao convite = conviteValido();
        convite.getUsuario().setAtivo(true);

        verificarRejeicaoSemAlteracoes(convite);
    }

    @Test
    void deveRecusarConviteDeUsuarioComPrimeiroAcessoConcluido() {
        ConviteAtivacao convite = conviteValido();
        convite.getUsuario().setPrimeiroAcesso(false);

        verificarRejeicaoSemAlteracoes(convite);
    }

    @Test
    void deveRecusarConviteDeCoordenador() {
        ConviteAtivacao convite = conviteValido();
        convite.getUsuario().setPerfil(PerfilUsuario.COORDENADOR);

        verificarRejeicaoSemAlteracoes(convite);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"            ", "12345678901", "🔑🔑🔑🔑🔑a"})
    @MethodSource("senhasAcimaDoLimiteEmBytes")
    void deveRecusarSenhaInvalidaAntesDeConsultarConvite(String senha) {
        assertThrows(IllegalArgumentException.class, () -> service.ativarConta(TOKEN, senha));
        verifyNoInteractions(convites, usuarios, encoder);
    }

    static Stream<String> senhasAcimaDoLimiteEmBytes() {
        return Stream.of("a".repeat(73), "é".repeat(36) + "a");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789012", "🔑🔑🔑🔑🔑🔑"})
    @MethodSource("senhasCom72Bytes")
    void deveAtivarContaEConsumirConviteComSenhaValida(String senha) {
        ConviteAtivacao convite = conviteValido();
        Usuario usuario = convite.getUsuario();

        service.ativarConta(TOKEN, senha);

        verify(usuarios).save(same(usuario));
        verify(convites).save(same(convite));
        assertTrue(usuario.isAtivo());
        assertFalse(usuario.isPrimeiroAcesso());
        assertNotNull(usuario.getSenhaHash());
        assertNotEquals(senha, usuario.getSenhaHash());
        assertTrue(new BCryptPasswordEncoder().matches(senha, usuario.getSenhaHash()));
        assertNotNull(convite.getUtilizadoEm());
        assertFalse(convite.getUtilizadoEm().isBefore(convite.getCriadoEm()));
        assertTrue(convite.getUtilizadoEm().isBefore(convite.getExpiraEm()));
    }

    static Stream<String> senhasCom72Bytes() {
        return Stream.of("a".repeat(72), "é".repeat(36));
    }

    @Test
    void devePropagarFalhaDeCriptografiaSemAlterarContaOuConvite() {
        ConviteAtivacao convite = conviteValido();
        IllegalStateException falha = new IllegalStateException("Falha no BCrypt");
        doThrow(falha).when(encoder).encode(SENHA);

        assertSame(falha, assertThrows(IllegalStateException.class,
                () -> service.ativarConta(TOKEN, SENHA)));

        assertNull(convite.getUsuario().getSenhaHash());
        assertFalse(convite.getUsuario().isAtivo());
        assertTrue(convite.getUsuario().isPrimeiroAcesso());
        assertNull(convite.getUtilizadoEm());
        verifyNoInteractions(usuarios);
        verify(convites, never()).save(any());
    }

    @Test
    void devePropagarFalhaAoSalvarUsuarioSemProsseguirParaGravacaoDoConvite() {
        ConviteAtivacao convite = conviteValido();
        DataIntegrityViolationException falha = new DataIntegrityViolationException("Falha no usuário");
        when(usuarios.save(convite.getUsuario())).thenThrow(falha);

        assertSame(falha, assertThrows(DataIntegrityViolationException.class,
                () -> service.ativarConta(TOKEN, SENHA)));
        verify(convites, never()).save(any());
    }

    @Test
    void devePropagarFalhaAoGravarUsoDoConvite() {
        ConviteAtivacao convite = conviteValido();
        DataIntegrityViolationException falha = new DataIntegrityViolationException("Falha no convite");
        when(convites.save(convite)).thenThrow(falha);

        assertSame(falha, assertThrows(DataIntegrityViolationException.class,
                () -> service.ativarConta(TOKEN, SENHA)));
    }

    private ConviteAtivacao conviteValido() {
        ConviteAtivacao convite = new ConviteAtivacao();
        LocalDateTime agora = LocalDateTime.now();
        convite.setUsuario(novoFuncionario());
        convite.setTokenHash(HASH_TOKEN);
        convite.setCriadoEm(agora.minusHours(1));
        convite.setExpiraEm(agora.plusHours(23));
        when(convites.buscarPorTokenHashComBloqueio(HASH_TOKEN)).thenReturn(Optional.of(convite));
        return convite;
    }

    private Usuario novoFuncionario() {
        Usuario usuario = new Usuario();
        usuario.setId(8L);
        usuario.setPerfil(PerfilUsuario.FUNCIONARIO);
        usuario.setAtivo(false);
        usuario.setPrimeiroAcesso(true);
        return usuario;
    }

    private void verificarRejeicaoSemAlteracoes(ConviteAtivacao convite) {
        Usuario usuario = convite.getUsuario();
        boolean ativo = usuario.isAtivo();
        boolean primeiroAcesso = usuario.isPrimeiroAcesso();
        LocalDateTime utilizadoEm = convite.getUtilizadoEm();

        assertThrows(ConviteInvalidoException.class, () -> service.ativarConta(TOKEN, SENHA));

        assertEquals(ativo, usuario.isAtivo());
        assertEquals(primeiroAcesso, usuario.isPrimeiroAcesso());
        assertNull(usuario.getSenhaHash());
        assertEquals(utilizadoEm, convite.getUtilizadoEm());
        verify(convites, never()).save(any());
        verifyNoInteractions(usuarios, encoder);
    }
}
