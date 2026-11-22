package br.edu.upe.sigep.integration;

import br.edu.upe.sigep.dto.request.CoordenadorCredenciaisRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioAtualizacaoRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.exception.EmailJaCadastradoException;
import br.edu.upe.sigep.service.UsuarioService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static br.edu.upe.sigep.enums.PerfilUsuario.COORDENADOR;
import static br.edu.upe.sigep.enums.PerfilUsuario.FUNCIONARIO;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioCadastroIT extends IntegracaoUsuarios {

    private static final String EMAIL = "funcionario@sigep.example";
    private static final UsuarioCadastroRequestDTO DADOS =
            new UsuarioCadastroRequestDTO(" Funcionário Teste ", " FUNCIONARIO@SIGEP.EXAMPLE ", SENHA);

    @Autowired
    private UsuarioService service;

    @Autowired
    private MockMvc mvc;

    @Test
    void persisteCadastroEPermiteLoginAteDesativarMantendoReferencias() throws Exception {
        Usuario coordenador = criarCoordenador();
        var cadastro = service.cadastrarFuncionario(DADOS, coordenador.getId());
        Long id = cadastro.id();
        transacoes.executeWithoutResult(status -> {
            Usuario salvo = usuarios.findById(id).orElseThrow();
            assertEquals("Funcionário Teste", salvo.getNome());
            assertEquals(EMAIL, salvo.getEmail());
            assertEquals(FUNCIONARIO, salvo.getPerfil());
            assertTrue(salvo.isAtivo());
            assertTrue(encoder.matches(SENHA, salvo.getSenhaHash()));
            assertEquals(coordenador.getId(), salvo.getCriadoPor().getId());
            jdbc.update("""
                    insert into auditoria(usuario_id, tabela_nome, registro_id, acao, criado_em)
                    values (?, 'usuario', ?, 'TESTE_CRUD_USUARIO', now())
                    """, id, id);
        });

        var sessao = entrar(EMAIL, SENHA, "/");
        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(status().isOk());

        service.atualizarFuncionario(id, new UsuarioAtualizacaoRequestDTO("Outro nome", EMAIL));
        var sessaoCoordenador = entrar(coordenador.getEmail(), SENHA, "/");
        assertNotEquals(sessao.getId(), sessaoCoordenador.getId());
        mvc.perform(delete("/api/usuarios/{id}", id).session(sessaoCoordenador).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/usuarios/{id}", id).session(sessaoCoordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
        assertFalse(sessao.isInvalid());
        assertFalse(service.buscarPorId(id).ativo());
        assertEquals("Outro nome", service.buscarPorId(id).nome());
        assertEquals(coordenador.getId(), service.buscarPorId(id).criadoPorId());
        assertEquals(id, jdbc.queryForObject(
                "select usuario_id from auditoria where acao = 'TESTE_CRUD_USUARIO'", Long.class));

        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(redirectedUrl("/login"));
        assertTrue(sessao.isInvalid());
        entrar(EMAIL, SENHA, "/login?error");
    }

    @Test
    void reverteCadastroAposFalhaNaTransacao() {
        Long coordenadorId = criarCoordenador().getId();
        assertThrows(IllegalStateException.class, () -> transacoes.executeWithoutResult(status -> {
            service.cadastrarFuncionario(DADOS, coordenadorId);
            assertTrue(usuarios.findByEmail(EMAIL).isPresent());
            throw new IllegalStateException("Falha deliberada.");
        }));

        assertTrue(usuarios.findByEmail(EMAIL).isEmpty());
        assertTrue(usuarios.existsById(coordenadorId));
    }

    @Test
    void protegeUnicidadeDeEmailECoordenadorNoBanco() {
        Long coordenadorId = criarCoordenador().getId();
        service.cadastrarFuncionario(DADOS, coordenadorId);
        assertThrows(EmailJaCadastradoException.class, () -> service.cadastrarFuncionario(DADOS, coordenadorId));
        assertThrows(DataIntegrityViolationException.class, () -> inserirUsuario("FUNCIONARIO@SIGEP.EXAMPLE", "FUNCIONARIO"));
        assertThrows(DataIntegrityViolationException.class, () -> inserirUsuario("outro@sigep.example", "COORDENADOR"));
        assertEquals(2, usuarios.count());
    }

    @Test
    void atualizaCoordenadorSemAlterarChavesERevogaSessoesMesmoComEmailIgual() throws Exception {
        Usuario coordenador = criarCoordenador();
        long id = coordenador.getId();
        var criadoEm = service.buscarPorId(id).criadoEm();
        var funcionario = service.cadastrarFuncionario(DADOS, id);
        var sessao = entrar(coordenador.getEmail(), SENHA, "/");
        String novaSenha = "nova-senha-ficticia";

        var sessaoDaTroca = entrar(coordenador.getEmail(), SENHA, "/");
        assertNotEquals(sessao.getId(), sessaoDaTroca.getId());
        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(status().isOk());

        mvc.perform(put("/api/usuarios/coordenador/credenciais").session(sessaoDaTroca).with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Coordenação atualizada","email":"%s","senhaAtual":"%s","novaSenha":"%s"}
                                """.formatted(coordenador.getEmail(), SENHA, novaSenha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senhaAtual").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.novaSenha").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.senhaHash").doesNotHaveJsonPath());
        assertTrue(sessaoDaTroca.isInvalid());
        assertFalse(sessao.isInvalid());

        var atualizado = service.buscarPorId(id);
        assertEquals(id, atualizado.id());
        assertEquals(COORDENADOR, atualizado.perfil());
        assertTrue(atualizado.ativo());
        assertEquals(criadoEm, atualizado.criadoEm());
        assertEquals(id, service.buscarPorId(funcionario.id()).criadoPorId());

        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(redirectedUrl("/login"));
        assertTrue(sessao.isInvalid());
        entrar(coordenador.getEmail(), SENHA, "/login?error");
        var novaSessao = entrar(coordenador.getEmail(), novaSenha, "/");
        mvc.perform(get("/api/auth/me").session(novaSessao)).andExpect(status().isOk());

        var outroEmail = "nova-coordenacao@sigep.example";
        service.atualizarCredenciais(id, new CoordenadorCredenciaisRequestDTO(
                atualizado.nome(), outroEmail, novaSenha, SENHA));
        assertTrue(usuarios.findByEmail(coordenador.getEmail()).isEmpty());
        assertEquals(id, usuarios.findByEmail(outroEmail).orElseThrow().getId());
        assertEquals(id, service.buscarPorId(funcionario.id()).criadoPorId());
    }

    private void inserirUsuario(String email, String perfil) {
        jdbc.update("""
                insert into usuario(nome, email, senha_hash, perfil, ativo, criado_em)
                values ('Teste', ?, ?, ?, true, now())
                """, email, encoder.encode(SENHA), perfil);
    }

    private MockHttpSession entrar(String email, String senha, String destino) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("email", email).param("password", senha))
                .andExpect(status().isFound()).andExpect(redirectedUrl(destino))
                .andReturn().getRequest().getSession(false);
    }

    private RequestPostProcessor csrf() throws Exception {
        var resposta = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn().getResponse();
        String token = JsonPath.read(resposta.getContentAsString(), "$.token");
        return request -> {
            request.setCookies(resposta.getCookie("XSRF-TOKEN"));
            request.addHeader("X-XSRF-TOKEN", token);
            return request;
        };
    }
}
