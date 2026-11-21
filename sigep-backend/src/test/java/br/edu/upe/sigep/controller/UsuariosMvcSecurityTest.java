package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.config.PasswordConfig;
import br.edu.upe.sigep.config.SecurityConfig;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.exception.ConviteInvalidoException;
import br.edu.upe.sigep.exception.EmailJaCadastradoException;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.CadastroFuncionarioResultado;
import br.edu.upe.sigep.service.ConviteAtivacaoService;
import br.edu.upe.sigep.service.UsuarioDetailsService;
import br.edu.upe.sigep.service.UsuarioService;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Optional;

import static br.edu.upe.sigep.enums.PerfilUsuario.COORDENADOR;
import static br.edu.upe.sigep.enums.PerfilUsuario.FUNCIONARIO;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({UsuarioController.class, AtivacaoContaController.class, AuthController.class, CsrfController.class})
@Import({
        UsuariosMvcSecurityTest.HabilitarSeguranca.class,
        SecurityConfig.class, PasswordConfig.class, UsuarioDetailsService.class
})
class UsuariosMvcSecurityTest {

    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class HabilitarSeguranca {
    }

    private static final String USUARIOS = "/api/usuarios";
    private static final String ATIVACAO = "/api/ativacao";
    private static final String LOGIN = "/api/auth/login";
    private static final String LOGOUT = "/api/auth/logout";
    private static final String ME = "/api/auth/me";
    private static final String CSRF = "/api/auth/csrf";
    private static final String EMAIL = "pessoa@sigep.example";
    private static final String SENHA = "senha-ficticia";
    private static final String TOKEN = "convite-ficticio";
    private static final String CADASTRO = """
            {"nome":"Pessoa Teste","email":"funcionario@sigep.example"}
            """;
    private static final String ATIVACAO_VALIDA = """
            {"token":"convite-ficticio","senha":"senha-ficticia"}
            """;
    private static final UsuarioResponseDTO FUNCIONARIO_PENDENTE = new UsuarioResponseDTO(
            8L, "Pessoa Teste", "funcionario@sigep.example", FUNCIONARIO, false, true, 1L, null
    );

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder encoder;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private ConviteAtivacaoService conviteService;

    @Test
    void deveCadastrarFuncionarioComSessaoDeCoordenadorECsrf() throws Exception {
        Usuario coordenador = configurarUsuario(COORDENADOR, true);
        MockHttpSession sessao = autenticar();
        when(usuarioService.cadastrarFuncionario(dadosCadastro(), coordenador))
                .thenReturn(new CadastroFuncionarioResultado(FUNCIONARIO_PENDENTE, TOKEN));

        mvc.perform(cadastro().session(sessao).with(obterCsrf().enviar()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.nome").value("Pessoa Teste"))
                .andExpect(jsonPath("$.email").value("funcionario@sigep.example"))
                .andExpect(jsonPath("$.perfil").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.primeiroAcesso").value(true))
                .andExpect(jsonPath("$.criadoPorId").value(1))
                .andExpect(jsonPath("$.senha").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.senhaHash").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.token").doesNotHaveJsonPath())
                .andExpect(content().string(not(containsString(TOKEN))));

        verify(usuarioService).cadastrarFuncionario(dadosCadastro(), coordenador);
    }

    @Test
    void deveNegarCadastroDeFuncionarioMesmoComCsrfValido() throws Exception {
        configurarUsuario(FUNCIONARIO, true);

        mvc.perform(cadastro().session(autenticar()).with(obterCsrf().enviar()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRedirecionarCadastroAnonimoMesmoComCsrfValido() throws Exception {
        mvc.perform(cadastro().with(obterCsrf().enviar()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(usuarioService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void deveNegarCadastroSemCsrfValido(boolean enviarTokenInvalido) throws Exception {
        configurarUsuario(COORDENADOR, true);
        MockHttpServletRequestBuilder requisicao = cadastro().session(autenticar());
        if (enviarTokenInvalido) {
            CsrfEmitido csrf = obterCsrf();
            requisicao.cookie(csrf.cookie()).header(csrf.cabecalho(), "token-invalido");
        }

        mvc.perform(requisicao).andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveResponderConflitoParaEmailDuplicado() throws Exception {
        Usuario coordenador = configurarUsuario(COORDENADOR, true);
        String erro = "Já existe um usuário cadastrado com esse e-mail.";
        when(usuarioService.cadastrarFuncionario(dadosCadastro(), coordenador))
                .thenThrow(new EmailJaCadastradoException(erro));

        mvc.perform(cadastro().session(autenticar()).with(obterCsrf().enviar()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(erro));
    }

    @Test
    void deveResponderBadRequestParaCadastroInvalido() throws Exception {
        configurarUsuario(COORDENADOR, true);

        mvc.perform(post(USUARIOS).session(autenticar()).with(obterCsrf().enviar())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":" ","email":"invalido"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nome").value("O nome é obrigatório"))
                .andExpect(jsonPath("$.email").value("Informe um e-mail válido"));

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveBuscarUsuarioPorIdComoCoordenador() throws Exception {
        configurarUsuario(COORDENADOR, true);
        when(usuarioService.buscarPorId(8L)).thenReturn(FUNCIONARIO_PENDENTE);

        mvc.perform(get(USUARIOS + "/8").session(autenticar()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.email").value("funcionario@sigep.example"))
                .andExpect(jsonPath("$.perfil").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.senhaHash").doesNotHaveJsonPath());
    }

    @Test
    void deveResponderBadRequestQuandoUsuarioNaoExiste() throws Exception {
        configurarUsuario(COORDENADOR, true);
        when(usuarioService.buscarPorId(99L))
                .thenThrow(new IllegalArgumentException("Usuário não encontrado: 99"));

        mvc.perform(get(USUARIOS + "/99").session(autenticar()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado: 99"));
    }

    @Test
    void deveListarUsuariosComoCoordenador() throws Exception {
        configurarUsuario(COORDENADOR, true);
        when(usuarioService.listarTodos()).thenReturn(List.of(FUNCIONARIO_PENDENTE));

        mvc.perform(get(USUARIOS).session(autenticar()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].ativo").value(false))
                .andExpect(jsonPath("$[0].criadoPorId").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {USUARIOS, USUARIOS + "/8"})
    void deveNegarConsultasDeUsuariosParaFuncionario(String rota) throws Exception {
        configurarUsuario(FUNCIONARIO, true);

        mvc.perform(get(rota).session(autenticar())).andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveAtivarContaAnonimaComCsrfESemCorpoNaResposta() throws Exception {
        mvc.perform(ativacao(ATIVACAO_VALIDA).with(obterCsrf().enviar()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(conviteService).ativarConta(TOKEN, SENHA);
    }

    @Test
    void deveResponderBadRequestParaConviteInvalido() throws Exception {
        String erro = "Convite inválido ou expirado.";
        doThrow(new ConviteInvalidoException(erro)).when(conviteService).ativarConta(TOKEN, SENHA);

        mvc.perform(ativacao(ATIVACAO_VALIDA).with(obterCsrf().enviar()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(erro));
    }

    @Test
    void deveValidarComprimentoMinimoDaSenhaNaAtivacao() throws Exception {
        mvc.perform(ativacao("""
                        {"token":"convite-ficticio","senha":"12345678901"}
                        """).with(obterCsrf().enviar()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.senha").isNotEmpty());

        verifyNoInteractions(conviteService);
    }

    @Test
    void deveExigirTokenESenhaNaAtivacao() throws Exception {
        mvc.perform(ativacao("{}").with(obterCsrf().enviar()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").value("O token é obrigatório"))
                .andExpect(jsonPath("$.senha").value("A senha é obrigatória"));

        verifyNoInteractions(conviteService);
    }

    @Test
    void deveNegarAtivacaoSemCsrf() throws Exception {
        mvc.perform(ativacao(ATIVACAO_VALIDA)).andExpect(status().isForbidden());

        verifyNoInteractions(conviteService);
    }

    @Test
    void deveEmitirTokenECookieCsrfParaAnonimo() throws Exception {
        CsrfEmitido csrf = obterCsrf();

        assertFalse(csrf.token().isBlank());
    }

    @ParameterizedTest
    @EnumSource(PerfilUsuario.class)
    void deveAutenticarEChecarIdentidadeDaSessao(PerfilUsuario perfil) throws Exception {
        configurarUsuario(perfil, true);

        mvc.perform(get(ME).session(autenticar()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.permissoes", containsInAnyOrder(
                        "ROLE_" + perfil.name(), "FACTOR_PASSWORD"
                )));
    }

    @ParameterizedTest
    @ValueSource(strings = {ME, USUARIOS, USUARIOS + "/8"})
    void deveRedirecionarConsultaAnonimaParaLogin(String rota) throws Exception {
        mvc.perform(get(rota))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void deveRedirecionarParaLoginComErroQuandoSenhaIncorreta() throws Exception {
        configurarUsuario(COORDENADOR, true);

        verificarFalhaDeLogin("senha-incorreta");
    }

    @Test
    void deveRedirecionarParaLoginComErroQuandoContaInativa() throws Exception {
        configurarUsuario(COORDENADOR, false);

        verificarFalhaDeLogin(SENHA);
    }

    @Test
    void deveNegarLoginSemCsrfAntesDeConsultarCredenciais() throws Exception {
        mvc.perform(login(SENHA)).andExpect(status().isForbidden());

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveEncerrarSessaoComCsrfERedirecionarAcessoPosterior() throws Exception {
        configurarUsuario(COORDENADOR, true);
        MockHttpSession sessao = autenticar();
        CsrfEmitido csrf = obterCsrf();

        mvc.perform(post(LOGOUT).session(sessao).with(csrf.enviar()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0));
        assertTrue(sessao.isInvalid());

        mvc.perform(get(ME).cookie(csrf.cookie()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void devePreservarSessaoSeLogoutForNegadoPorFaltaDeCsrf() throws Exception {
        configurarUsuario(COORDENADOR, true);
        MockHttpSession sessao = autenticar();

        mvc.perform(post(LOGOUT).session(sessao)).andExpect(status().isForbidden());
        mvc.perform(get(ME).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true));
    }

    @Test
    void deveNegarRotaForaDaListaMesmoParaCoordenador() throws Exception {
        configurarUsuario(COORDENADOR, true);

        mvc.perform(get("/api/pacientes").session(autenticar())).andExpect(status().isForbidden());
    }

    private MockHttpSession autenticar() throws Exception {
        MockHttpSession sessao = (MockHttpSession) mvc.perform(login(SENHA).with(obterCsrf().enviar()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0))
                .andReturn().getRequest().getSession(false);
        assertNotNull(sessao);
        return sessao;
    }

    private void verificarFalhaDeLogin(String senha) throws Exception {
        MockHttpSession sessao = (MockHttpSession) mvc.perform(login(senha).with(obterCsrf().enviar()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn().getRequest().getSession(false);
        MockHttpServletRequestBuilder consulta = get(ME);
        if (sessao != null) {
            consulta.session(sessao);
        }
        mvc.perform(consulta)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    private CsrfEmitido obterCsrf() throws Exception {
        var resposta = mvc.perform(get(CSRF))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.parameterName").value("_csrf"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse();
        var json = JsonPath.parse(resposta.getContentAsString());
        return new CsrfEmitido(resposta.getCookie("XSRF-TOKEN"),
                json.read("$.headerName", String.class), json.read("$.token", String.class));
    }

    private Usuario configurarUsuario(PerfilUsuario perfil, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(EMAIL);
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);
        usuario.setSenhaHash(encoder.encode(SENHA));
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    private MockHttpServletRequestBuilder login(String senha) {
        return post(LOGIN).contentType(APPLICATION_FORM_URLENCODED)
                .param("email", EMAIL)
                .param("password", senha);
    }

    private MockHttpServletRequestBuilder cadastro() {
        return post(USUARIOS).contentType(APPLICATION_JSON).content(CADASTRO);
    }

    private MockHttpServletRequestBuilder ativacao(String corpo) {
        return post(ATIVACAO).contentType(APPLICATION_JSON).content(corpo);
    }

    private UsuarioCadastroRequestDTO dadosCadastro() {
        return new UsuarioCadastroRequestDTO("Pessoa Teste", "funcionario@sigep.example");
    }

    private record CsrfEmitido(Cookie cookie, String cabecalho, String token) {

        RequestPostProcessor enviar() {
            return request -> {
                request.setCookies(cookie);
                request.addHeader(cabecalho, token);
                return request;
            };
        }
    }
}
