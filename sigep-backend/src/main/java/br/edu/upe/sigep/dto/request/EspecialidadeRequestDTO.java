package br.edu.upe.sigep.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EspecialidadeRequestDTO(
        @NotBlank
        @Size(max = 150)
        String nome,

        @NotBlank
        @Size(max = 255)
        String descricao
) {
}
