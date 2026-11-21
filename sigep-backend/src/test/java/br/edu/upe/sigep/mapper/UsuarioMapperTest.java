package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void deveMapearDadosDoFuncionarioEIdDoCriador() {
        LocalDateTime criadoEm = LocalDateTime.of(2026, 1, 15, 10, 0);
        Usuario criador = new Usuario();
        criador.setId(3L);
        Usuario usuario = new Usuario(
                8L, "Pessoa Teste", "pessoa@sigep.example", null,
                PerfilUsuario.FUNCIONARIO, false, true, criador, criadoEm
        );

        assertEquals(new UsuarioResponseDTO(
                8L, "Pessoa Teste", "pessoa@sigep.example", PerfilUsuario.FUNCIONARIO,
                false, true, 3L, criadoEm
        ), mapper.toResponseDTO(usuario));
    }

    @Test
    void deveMapearCoordenadorSemCriador() {
        LocalDateTime criadoEm = LocalDateTime.of(2026, 1, 15, 10, 0);
        Usuario usuario = new Usuario(
                3L, "Coordenacao", "coordenacao@sigep.example", "hash-ficticio",
                PerfilUsuario.COORDENADOR, true, false, null, criadoEm
        );

        assertEquals(new UsuarioResponseDTO(
                3L, "Coordenacao", "coordenacao@sigep.example", PerfilUsuario.COORDENADOR,
                true, false, null, criadoEm
        ), mapper.toResponseDTO(usuario));
    }

    @Test
    void deveRetornarNuloParaUsuarioNulo() {
        assertNull(mapper.toResponseDTO(null));
    }
}
