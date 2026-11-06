package br.edu.upe.sigep.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfissionalRequestDTO(
        @NotBlank
        @Size(max = 200)
        String nome,

        @NotBlank
        @Size(max = 200)
        String conselho,

        @NotBlank
        @Size(max = 50)
        String registroConselho,

        Long cadastradoPorId
) {
}
