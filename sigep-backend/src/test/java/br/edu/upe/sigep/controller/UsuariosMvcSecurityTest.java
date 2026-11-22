package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.config.PasswordConfig;
import br.edu.upe.sigep.config.SecurityConfig;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioAtualizacaoRequestDTO;
import br.edu.upe.sigep.dto.request.CoordenadorCredenciaisRequestDTO;
import br.edu.upe.sigep.exception.OperacaoNaoPermitidaException;
import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.exception.EmailJaCadastradoException;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.UsuarioDetailsService;
import br.edu.upe.sigep.service.UsuarioService;
import br.edu.upe.sigep.service.impl.AgendamentoSessaoService;
import br.edu.upe.sigep.service.impl.AuditoriaService;
import br.edu.upe.sigep.service.impl.CotaMensalService;
import br.edu.upe.sigep.service.impl.EscalaService;
import br.edu.upe.sigep.service.impl.EspecialidadeService;
import br.edu.upe.sigep.service.impl.PacienteService;
import br.edu.upe.sigep.service.impl.PlanoTratamentoService;
import br.edu.upe.sigep.service.impl.ProfissionalService;
import br.edu.upe.sigep.service.impl.SolicitacaoAgendamentoService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static br.edu.upe.sigep.enums.PerfilUsuario.COORDENADOR;
import static br.edu.upe.sigep.enums.PerfilUsuario.FUNCIONARIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        UsuarioController.class, AuthController.class, CsrfController.class,
        PacienteController.class, ProfissionalController.class, EspecialidadeController.class,
        SolicitacaoAgendamentoController.class, PlanoTratamentoController.class, AgendamentoSessaoController.class,
        EscalaController.class, CotaMensalController.class, AuditoriaController.class
})
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
    private static final String LOGIN = "/api/auth/login";
    private static final String LOGOUT = "/api/auth/logout";
    private static final String ME = "/api/auth/me";
    private static final String CSRF = "/api/auth/csrf";
    private static final String EMAIL = "pessoa@sigep.example";
    private static final String SENHA = "senha-ficticia";
    private static final String CADASTRO = """
            {"nome":"Pessoa Teste","email":"funcionario@sigep.example","senha":"senha-ficticia"}
            """;
    private static final UsuarioResponseDTO FUNCIONARIO_CADASTRADO = new UsuarioResponseDTO(
            8L, "Pessoa Teste", "funcionario@sigep.example", FUNCIONARIO, true, 1L, null
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
    private PacienteService pacienteService;

    @MockitoBean
    private ProfissionalService profissionalService;

    @MockitoBean
    private EspecialidadeService especialidadeService;

    @MockitoBean
    private SolicitacaoAgendamentoService solicitacaoAgendamentoService;

    @MockitoBean
    private PlanoTratamentoService planoTratamentoService;

    @MockitoBean
    private AgendamentoSessaoService agendamentoSessaoService;

    @MockitoBean
    private EscalaService escalaService;

    @MockitoBean
    private CotaMensalService cotaMensalService;

    @MockitoBean
    private AuditoriaService auditoriaService;

    @Test
    void deveCadastrarFuncionarioComSessaoDeCoordenadorECsrf() throws Exception {
        configurarUsuario(COORDENADOR, true);
        MockHttpSession sessao = autenticar();
        when(usuarioService.cadastrarFuncionario(dadosCadastro(), 1L))
                .thenReturn(FUNCIONARIO_CADASTRADO);

        mvc.perform(cadastro().session(sessao).with(obterCsrf().enviar()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.nome").value("Pessoa Teste"))
                .andExpect(jsonPath("$.email").value("funcionario@sigep.example"))
                .andExpect(jsonPath("$.perfil").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.criadoPorId").value(1))
                .andExpect(jsonPath("$.senha").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.senhaHash").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.token").doesNotHaveJsonPath());

        verify(usuarioService).cadastrarFuncionario(dadosCadastro(), 1L);
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
        configurarUsuario(COORDENADOR, true);
        String erro = "Já existe um usuário cadastrado com esse e-mail.";
        when(usuarioService.cadastrarFuncionario(dadosCadastro(), 1L))
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
                .andExpect(jsonPath("$.email").value("Informe um e-mail válido"))
                .andExpect(jsonPath("$.senha").isNotEmpty());

        verifyNoInteractions(usuarioService);
    }

    @ParameterizedTest
    @ValueSource(strings = {USUARIOS, USUARIOS + "/coordenador/credenciais"})
    void deveResponderJsonInvalidoSemEcoarCredenciais(String rota) throws Exception {
        configurarUsuario(COORDENADOR, true);
        boolean cadastro = rota.equals(USUARIOS);
        var requisicao = cadastro ? post(rota) : put(rota);
        String json = cadastro
                ? """
                  {"senha":segredo-invalido}
                  """
                : """
                  {"senhaAtual":"segredo-atual","novaSenha":segredo-invalido}
                  """;

        mvc.perform(requisicao.session(autenticar()).with(obterCsrf().enviar())
                        .contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"erro":"Corpo JSON inválido."}
                        """))
                .andExpect(resultado -> assertThat(resultado.getResolvedException())
                        .hasMessage("Corpo JSON inválido.")
                        .hasNoCause());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveBuscarUsuarioPorIdComoCoordenador() throws Exception {
        configurarUsuario(COORDENADOR, true);
        when(usuarioService.buscarPorId(8L)).thenReturn(FUNCIONARIO_CADASTRADO);

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
        when(usuarioService.listarTodos()).thenReturn(List.of(FUNCIONARIO_CADASTRADO));

        mvc.perform(get(USUARIOS).session(autenticar()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].ativo").value(true))
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
        configurarUsuario(FUNCIONARIO, false);

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

    @ParameterizedTest
    @EnumSource(PerfilUsuario.class)
    void devePermitirConsultasNosModulosDeNegocio(PerfilUsuario perfil) throws Exception {
        configurarUsuario(perfil, true);
        var sessao = autenticar();

        for (String rota : List.of("/api/pacientes", "/api/profissionais", "/api/especialidades",
                "/api/solicitacoes", "/api/planos-tratamento", "/api/agendamentos-sessoes", "/api/escalas")) {
            mvc.perform(get(rota).session(sessao)).andExpect(status().isOk());
        }
        mvc.perform(get("/api/cotas-mensais/disponibilidade").session(sessao)
                        .param("especialidadeId", "1").param("data", "2026-11-21"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(PerfilUsuario.class)
    void devePermitirMutacoesNosModulosDeNegocioComCsrf(PerfilUsuario perfil) throws Exception {
        configurarUsuario(perfil, true);
        var sessao = autenticar();
        var csrf = obterCsrf();

        mvc.perform(delete("/api/pacientes/1").session(sessao).with(csrf.enviar()))
                .andExpect(status().isNoContent());
        mvc.perform(patch("/api/cotas-mensais/decrementar").session(sessao).with(csrf.enviar())
                        .param("especialidadeId", "1").param("data", "2026-11-21"))
                .andExpect(status().isNoContent());

        verify(pacienteService).inativar(1L);
        verify(cotaMensalService).decrementarCota(1L, LocalDate.of(2026, 11, 21));
    }

    @Test
    void deveRestringirAuditoriaAoCoordenador() throws Exception {
        configurarUsuario(COORDENADOR, true);
        mvc.perform(get("/api/auditorias/cronologica").session(autenticar()))
                .andExpect(status().isOk());
        verify(auditoriaService).listarCronologicamente();
        clearInvocations(auditoriaService);

        configurarUsuario(FUNCIONARIO, true);
        var sessao = autenticar();
        mvc.perform(get("/api/auditorias/cronologica").session(sessao))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auditorias").session(sessao).with(obterCsrf().enviar()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(auditoriaService);
    }

    @Test
    void deveExigirSessaoNasRotasDeNegocio() throws Exception {
        mvc.perform(get("/api/pacientes")).andExpect(redirectedUrl("/login"));
        mvc.perform(delete("/api/pacientes/1").with(obterCsrf().enviar()))
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(pacienteService);
    }

    @Test
    void deveRejeitarMutacaoDeNegocioSemCsrfValido() throws Exception {
        configurarUsuario(FUNCIONARIO, true);
        var sessao = autenticar();

        mvc.perform(delete("/api/pacientes/1").session(sessao))
                .andExpect(status().isForbidden());
        var csrf = obterCsrf();
        mvc.perform(delete("/api/pacientes/1").session(sessao)
                        .cookie(csrf.cookie()).header(csrf.cabecalho(), obterCsrf().token()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(pacienteService);
    }

    @Test
    void deveNegarRotasEMetodosForaDaListaMesmoParaCoordenador() throws Exception {
        configurarUsuario(COORDENADOR, true);
        var sessao = autenticar();

        mvc.perform(get("/api/pacientes/exportar").session(sessao)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/pacientes").session(sessao).with(obterCsrf().enviar()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(pacienteService);
    }

    @Test
    void deveAtualizarEDesativarFuncionarioComCsrf() throws Exception {
        configurarUsuario(COORDENADOR, true);
        var sessao = autenticar();
        var dados = new UsuarioAtualizacaoRequestDTO("Nome atualizado", "novo@sigep.example");
        when(usuarioService.atualizarFuncionario(8L, dados)).thenReturn(new UsuarioResponseDTO(
                8L, dados.nome(), dados.email(), FUNCIONARIO, true, 1L, null));

        mvc.perform(put(USUARIOS + "/8").session(sessao).with(obterCsrf().enviar())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Nome atualizado","email":"novo@sigep.example"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(dados.email()));

        mvc.perform(delete(USUARIOS + "/8").session(sessao).with(obterCsrf().enviar()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(usuarioService).desativarFuncionario(8L);
    }

    @Test
    void deveRetornar400ParaAtualizacaoEDesativacaoDeIdInexistente() throws Exception {
        configurarUsuario(COORDENADOR, true);
        var sessao = autenticar();
        var csrf = obterCsrf();
        when(usuarioService.atualizarFuncionario(eq(99L), any()))
                .thenThrow(new IllegalArgumentException("Usuário não encontrado: 99"));
        doThrow(new IllegalArgumentException("Usuário não encontrado: 99"))
                .when(usuarioService).desativarFuncionario(99L);

        mvc.perform(put(USUARIOS + "/99").session(sessao).with(csrf.enviar())
                        .contentType(APPLICATION_JSON).content("""
                                {"nome":"Nome","email":"teste@sigep.example"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(delete(USUARIOS + "/99").session(sessao).with(csrf.enviar()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExigirCsrfNasNovasRotas() throws Exception {
        configurarUsuario(COORDENADOR, true);
        var sessao = autenticar();
        for (var requisicao : List.of(put(USUARIOS + "/8"), delete(USUARIOS + "/8"), credenciais())) {
            mvc.perform(requisicao.session(sessao)).andExpect(status().isForbidden());
        }
        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveNegarMutacoesDeFuncionarioComCsrfValido() throws Exception {
        configurarUsuario(FUNCIONARIO, true);
        var sessao = autenticar();
        var csrf = obterCsrf();
        for (var requisicao : List.of(put(USUARIOS + "/8"), delete(USUARIOS + "/8"),
                put(USUARIOS + "/coordenador/credenciais"))) {
            mvc.perform(requisicao.session(sessao).with(csrf.enviar()))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveTrocarCredenciaisEEncerrarSessao() throws Exception {
        var coordenador = configurarUsuario(COORDENADOR, true);
        var sessao = autenticar();
        var outraSessao = autenticar();
        var csrf = obterCsrf();
        var dados = new CoordenadorCredenciaisRequestDTO("Coordenação", EMAIL, SENHA, "nova-senha-ficticia");
        when(usuarioService.atualizarCredenciais(1L, dados)).thenAnswer(chamada -> {
            coordenador.setRevisaoCredenciais(1);
            return new UsuarioResponseDTO(1L, dados.nome(), EMAIL, COORDENADOR, true, null, null);
        });

        mvc.perform(credenciais().session(sessao).with(csrf.enviar()))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.senhaHash").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.novaSenha").doesNotHaveJsonPath());
        assertTrue(sessao.isInvalid());
        mvc.perform(get(ME).session(outraSessao))
                .andExpect(redirectedUrl("/login"));
        assertTrue(outraSessao.isInvalid());
    }

    @Test
    void devePreservarSessaoQuandoSenhaAtualIncorreta() throws Exception {
        configurarUsuario(COORDENADOR, true);
        var sessao = autenticar();
        when(usuarioService.atualizarCredenciais(eq(1L), any()))
                .thenThrow(new OperacaoNaoPermitidaException("A senha atual está incorreta."));

        mvc.perform(credenciais().session(sessao).with(obterCsrf().enviar()))
                .andExpect(status().isForbidden());
        mvc.perform(get(ME).session(sessao)).andExpect(status().isOk());
    }

    @Test
    void deveInvalidarSessaoDeFuncionarioDesativado() throws Exception {
        var funcionario = configurarUsuario(FUNCIONARIO, true);
        var sessao = autenticar();
        funcionario.setAtivo(false);

        mvc.perform(delete("/api/pacientes/1").session(sessao).with(obterCsrf().enviar()))
                .andExpect(redirectedUrl("/login"));
        assertTrue(sessao.isInvalid());
        verifyNoInteractions(pacienteService);
    }

    @Test
    void deveRejeitarCookieSemCabecalhoOuComTokenDeOutroCookie() throws Exception {
        var csrf = obterCsrf();
        mvc.perform(login(SENHA).cookie(csrf.cookie())).andExpect(status().isForbidden());
        mvc.perform(login(SENHA).cookie(csrf.cookie()).header(csrf.cabecalho(), obterCsrf().token()))
                .andExpect(status().isForbidden());
        mvc.perform(login(SENHA).cookie(csrf.cookie()).header(csrf.cabecalho(), csrf.cookie().getValue()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveRejeitarTentativaDeDefinirPerfilNoCadastro() throws Exception {
        configurarUsuario(COORDENADOR, true);
        mvc.perform(post(USUARIOS).session(autenticar()).with(obterCsrf().enviar())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Teste","email":"teste@sigep.example","senha":"senha-ficticia","perfil":"COORDENADOR"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(usuarioService);
    }

    private MockHttpServletRequestBuilder credenciais() {
        return put(USUARIOS + "/coordenador/credenciais").contentType(APPLICATION_JSON)
                .content("""
                        {"nome":"Coordenação","email":"pessoa@sigep.example",
                         "senhaAtual":"senha-ficticia","novaSenha":"nova-senha-ficticia"}
                        """);
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
        when(usuarioRepository.buscarPorEmailNormalizado(EMAIL)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
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

    private UsuarioCadastroRequestDTO dadosCadastro() {
        return new UsuarioCadastroRequestDTO("Pessoa Teste", "funcionario@sigep.example", SENHA);
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
