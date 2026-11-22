package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.CoordenadorCredenciaisRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioAtualizacaoRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.exception.EmailJaCadastradoException;
import br.edu.upe.sigep.exception.OperacaoNaoPermitidaException;
import br.edu.upe.sigep.mapper.UsuarioMapper;
import br.edu.upe.sigep.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static br.edu.upe.sigep.enums.PerfilUsuario.COORDENADOR;
import static br.edu.upe.sigep.enums.PerfilUsuario.FUNCIONARIO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceImplTest {

    private static final String SENHA = "senha-ficticia";
    private static final String EMAIL = "funcionario@sigep.example";
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UsuarioServiceImpl service = new UsuarioServiceImpl(usuarios, new UsuarioMapper(), encoder);
    private final Usuario coordenador = new Usuario();
    private final Usuario funcionario = new Usuario();

    @BeforeEach
    void preparar() {
        coordenador.setId(1L);
        coordenador.setPerfil(COORDENADOR);
        coordenador.setEmail("coordenador@sigep.example");
        coordenador.setAtivo(true);
        coordenador.setSenhaHash(encoder.encode(SENHA));
        funcionario.setId(2L);
        funcionario.setPerfil(FUNCIONARIO);
        funcionario.setEmail(EMAIL);
        funcionario.setAtivo(true);
        funcionario.setCriadoPor(coordenador);
        funcionario.setSenhaHash(encoder.encode(SENHA));
        when(usuarios.findById(1L)).thenReturn(Optional.of(coordenador));
        when(usuarios.buscarParaAlteracao(1L)).thenReturn(Optional.of(coordenador));
        when(usuarios.buscarParaAlteracao(2L)).thenReturn(Optional.of(funcionario));
    }

    @ParameterizedTest
    @MethodSource("senhasValidas")
    void cadastraFuncionarioAtivoComBcryptECriador(String senha) {
        when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(chamada -> {
            Usuario salvo = chamada.getArgument(0);
            assertNotEquals(senha, salvo.getSenhaHash());
            assertTrue(encoder.matches(senha, salvo.getSenhaHash()));
            assertSame(coordenador, salvo.getCriadoPor());
            salvo.setId(2L);
            return salvo;
        });
        var resposta = service.cadastrarFuncionario(
                new UsuarioCadastroRequestDTO(" Nome ", " FUNCIONARIO@SIGEP.EXAMPLE ", senha), 1L);

        assertEquals(2L, resposta.id());
        assertEquals("Nome", resposta.nome());
        assertEquals(EMAIL, resposta.email());
        assertEquals(FUNCIONARIO, resposta.perfil());
        assertTrue(resposta.ativo());
        assertEquals(1L, resposta.criadoPorId());
    }

    static Stream<String> senhasValidas() {
        return Stream.of("123456789012", "é".repeat(36), "😀".repeat(12));
    }

    @ParameterizedTest
    @MethodSource("senhasInvalidas")
    void rejeitaSenhaInvalida(String senha) {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrarFuncionario(new UsuarioCadastroRequestDTO("Nome", EMAIL, senha), 1L));
        verify(usuarios, never()).saveAndFlush(any());
    }

    static Stream<String> senhasInvalidas() {
        return Stream.of(null, " ".repeat(12), "12345678901", "é".repeat(36) + "x", "😀".repeat(11));
    }

    @Test
    void rejeitaCamposObrigatoriosECoordenadorInativo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrarFuncionario(new UsuarioCadastroRequestDTO(" ", EMAIL, SENHA), 1L));
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrarFuncionario(new UsuarioCadastroRequestDTO("Nome", null, SENHA), 1L));
        coordenador.setAtivo(false);
        assertThrows(OperacaoNaoPermitidaException.class, () ->
                service.cadastrarFuncionario(new UsuarioCadastroRequestDTO("Nome", EMAIL, SENHA), 1L));
    }

    @Test
    void rejeitaEmailDuplicadoEmCadastroEAlteracoes() {
        when(usuarios.buscarPorEmailNormalizado(EMAIL)).thenReturn(Optional.of(funcionario));
        when(usuarios.buscarPorEmailNormalizado(coordenador.getEmail())).thenReturn(Optional.of(coordenador));
        assertThrows(EmailJaCadastradoException.class, () ->
                service.cadastrarFuncionario(new UsuarioCadastroRequestDTO("Nome", EMAIL, SENHA), 1L));
        assertThrows(EmailJaCadastradoException.class, () ->
                service.atualizarFuncionario(2L, new UsuarioAtualizacaoRequestDTO("Nome", coordenador.getEmail())));
        assertThrows(EmailJaCadastradoException.class, () ->
                service.atualizarCredenciais(1L, new CoordenadorCredenciaisRequestDTO("Nome", EMAIL, SENHA, SENHA)));
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test
    void consultaEListaIncluindoInativos() {
        funcionario.setAtivo(false);
        when(usuarios.findById(2L)).thenReturn(Optional.of(funcionario));
        when(usuarios.findAll()).thenReturn(List.of(coordenador, funcionario));
        var resposta = service.buscarPorId(2L);
        assertFalse(resposta.ativo());
        assertEquals(List.of(1L, 2L), service.listarTodos().stream().map(usuario -> usuario.id()).toList());
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(99L));
        assertThrows(IllegalArgumentException.class, () -> service.desativarFuncionario(99L));
        assertThrows(IllegalArgumentException.class, () ->
                service.atualizarFuncionario(99L, new UsuarioAtualizacaoRequestDTO("Nome", EMAIL)));
    }

    @Test
    void alteraNomeEEmailEDesativaSemApagar() {
        String hash = funcionario.getSenhaHash();
        when(usuarios.buscarPorEmailNormalizado(EMAIL)).thenReturn(Optional.of(funcionario));
        when(usuarios.saveAndFlush(funcionario)).thenReturn(funcionario);
        var atualizado = service.atualizarFuncionario(2L, new UsuarioAtualizacaoRequestDTO("Novo nome", EMAIL));
        assertEquals("Novo nome", atualizado.nome());
        assertEquals(EMAIL, atualizado.email());
        assertEquals(hash, funcionario.getSenhaHash());
        assertSame(coordenador, funcionario.getCriadoPor());

        service.desativarFuncionario(2L);
        service.desativarFuncionario(2L);
        assertFalse(funcionario.isAtivo());
        verify(usuarios, never()).deleteById(any());
        verify(usuarios, never()).delete(any());
    }

    @Test
    void protegeCoordenadorDasRotasDeFuncionario() {
        assertThrows(OperacaoNaoPermitidaException.class, () -> service.desativarFuncionario(1L));
        assertThrows(OperacaoNaoPermitidaException.class, () ->
                service.atualizarFuncionario(1L, new UsuarioAtualizacaoRequestDTO("Nome", EMAIL)));
        assertTrue(coordenador.isAtivo());
    }

    @Test
    void atualizaCredenciaisComConfirmacaoEPreservaIdentidade() {
        when(usuarios.saveAndFlush(coordenador)).thenReturn(coordenador);
        String novoEmail = "novo@sigep.example";
        String novaSenha = "nova-senha-ficticia";
        var atualizado = service.atualizarCredenciais(1L,
                new CoordenadorCredenciaisRequestDTO("Novo nome", novoEmail, SENHA, novaSenha));

        assertEquals(1L, atualizado.id());
        assertEquals(COORDENADOR, atualizado.perfil());
        assertTrue(atualizado.ativo());
        assertEquals(novoEmail, atualizado.email());
        assertEquals("Novo nome", atualizado.nome());
        assertTrue(encoder.matches(novaSenha, coordenador.getSenhaHash()));
        assertFalse(encoder.matches(SENHA, coordenador.getSenhaHash()));
        assertEquals(1L, coordenador.getRevisaoCredenciais());
    }

    @Test
    void rejeitaSenhaAtualIncorreta() {
        assertThrows(OperacaoNaoPermitidaException.class, () -> service.atualizarCredenciais(1L,
                new CoordenadorCredenciaisRequestDTO("Nome", EMAIL, "incorreta", SENHA)));
        assertEquals(0L, coordenador.getRevisaoCredenciais());
        verify(usuarios, never()).saveAndFlush(any());
    }
}
