
package br.edu.upe.sigep.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtivacaoContaRequestDTO(

        @NotBlank(message = "O token é obrigatório")
        String token,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 12, message = "A senha deve ter entre 12 e 72 caracteres")
        String senha

) {}
