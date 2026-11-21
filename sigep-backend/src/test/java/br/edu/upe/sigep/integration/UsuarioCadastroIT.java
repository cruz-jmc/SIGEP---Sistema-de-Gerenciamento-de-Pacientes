package br.edu.upe.sigep.integration;

import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.repository.ConviteAtivacaoRepository;
import br.edu.upe.sigep.service.CadastroFuncionarioResultado;
import br.edu.upe.sigep.service.TokenService;
import br.edu.upe.sigep.service.UsuarioService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static br.edu.upe.sigep.enums.PerfilUsuario.FUNCIONARIO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioCadastroIT extends IntegracaoUsuarios {

    private static final String EMAIL = "funcionario@sigep.example";
    private static final UsuarioCadastroRequestDTO DADOS =
            new UsuarioCadastroRequestDTO(" Funcionário Teste ", " FUNCIONARIO@SIGEP.EXAMPLE ");

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConviteAtivacaoRepository convites;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MockMvc mvc;

    @Test
    void devePersistirFuncionarioEConviteEEnviarEmailAposCommit() {
        Usuario coordenador = criarCoordenador();

        var resultado = Objects.requireNonNull(transacoes.execute(status -> {
            CadastroFuncionarioResultado cadastro = usuarioService.cadastrarFuncionario(DADOS, coordenador);
            verifyNoInteractions(mailSender);
            return cadastro;
        }));

        transacoes.executeWithoutResult(status -> {
            Usuario salvo = usuarios.findById(resultado.usuario().id()).orElseThrow();
            assertEquals("Funcionário Teste", salvo.getNome());
            assertEquals(EMAIL, salvo.getEmail());
            assertEquals(FUNCIONARIO, salvo.getPerfil());
            assertFalse(salvo.isAtivo());
            assertTrue(salvo.isPrimeiroAcesso());
            assertNull(salvo.getSenhaHash());
            assertEquals(coordenador.getId(), salvo.getCriadoPor().getId());

            var convite = convites.findByTokenHash(tokenService.gerarHash(resultado.tokenConvite())).orElseThrow();
            assertEquals(salvo.getId(), convite.getUsuario().getId());
            assertNotEquals(resultado.tokenConvite(), convite.getTokenHash());
            assertEquals(convite.getCriadoEm().plusHours(24), convite.getExpiraEm());
            assertNull(convite.getUtilizadoEm());
        });

        var mensagem = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mensagem.capture());
        assertArrayEquals(new String[]{EMAIL}, mensagem.getValue().getTo());
        assertTrue(Objects.requireNonNull(mensagem.getValue().getText()).lines()
                .anyMatch(resultado.tokenConvite()::equals));
    }

    @Test
    void deveReverterCadastroEConviteSemEnviarEmailQuandoTransacaoFalha() {
        Usuario coordenador = criarCoordenador();
        IllegalStateException falha = new IllegalStateException("Falha deliberada da transação de teste");

        assertSame(falha, assertThrows(IllegalStateException.class, () ->
                transacoes.executeWithoutResult(status -> {
                    usuarioService.cadastrarFuncionario(DADOS, coordenador);
                    assertEquals(2L, usuarios.count());
                    assertEquals(1L, convites.count());
                    verifyNoInteractions(mailSender);
                    throw falha;
                })));

        transacoes.executeWithoutResult(status -> {
            assertTrue(usuarios.findByEmail(EMAIL).isEmpty());
            assertTrue(usuarios.existsById(coordenador.getId()));
            assertEquals(0L, convites.count());
        });
        verifyNoInteractions(mailSender);
    }

    @Test
    void deveResponder201EManterCadastroQuandoEnvioPosCommitFalha() throws Exception {
        Usuario coordenador = criarCoordenador();
        doThrow(new MailSendException("Falha SMTP simulada"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        var respostaCsrf = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var csrf = JsonPath.parse(respostaCsrf.getContentAsString());
        var autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                coordenador.getEmail(), null, AuthorityUtils.createAuthorityList("ROLE_COORDENADOR")
        );
        var sessao = new MockHttpSession();
        sessao.setAttribute(SPRING_SECURITY_CONTEXT_KEY, new SecurityContextImpl(autenticacao));

        mvc.perform(post("/api/usuarios").session(sessao)
                        .cookie(Objects.requireNonNull(respostaCsrf.getCookie("XSRF-TOKEN")))
                        .header(csrf.read("$.headerName", String.class), csrf.read("$.token", String.class))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Funcionário Teste","email":"funcionario@sigep.example"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.ativo").value(false));

        verify(mailSender).send(any(SimpleMailMessage.class));
        transacoes.executeWithoutResult(status -> {
            Usuario salvo = usuarios.findByEmail(EMAIL).orElseThrow();
            var convitesSalvos = convites.findAll();
            assertFalse(salvo.isAtivo());
            assertNull(salvo.getSenhaHash());
            assertEquals(1, convitesSalvos.size());
            assertEquals(salvo.getId(), convitesSalvos.getFirst().getUsuario().getId());
            assertNull(convitesSalvos.getFirst().getUtilizadoEm());
        });
    }
}
