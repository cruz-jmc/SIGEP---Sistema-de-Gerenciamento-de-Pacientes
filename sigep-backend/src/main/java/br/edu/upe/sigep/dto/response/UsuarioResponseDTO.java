package br.edu.upe.sigep.dto.response;

import br.edu.upe.sigep.enums.PerfilUsuario;
import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        boolean ativo,
        boolean primeiroAcesso,
        Long criadoPorId,
        LocalDateTime criadoEm
) {}
