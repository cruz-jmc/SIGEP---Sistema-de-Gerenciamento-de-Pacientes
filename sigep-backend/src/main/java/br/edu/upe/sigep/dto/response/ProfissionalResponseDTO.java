package br.edu.upe.sigep.dto.response;

import java.time.LocalDateTime;

public record ProfissionalResponseDTO(
        Long id,
        String nome,
        String conselho,
        String registroConselho,
        boolean ativo,
        LocalDateTime criadoEm,
        Long cadastradoPorId
) {
}
