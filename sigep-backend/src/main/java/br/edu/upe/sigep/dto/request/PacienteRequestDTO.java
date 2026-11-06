package br.edu.upe.sigep.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteRequestDTO(
        @NotBlank
        @Size(max = 200)
        String nomeCompleto,

        @NotBlank
        @Size(max = 11)
        String cpf,

        @NotNull
        LocalDate dataNascimento,

        @NotBlank
        @Size(max = 15)
        String cartaoSus,

        @NotBlank
        @Size(max = 20)
        String telefone,

        @NotBlank
        @Size(max = 300)
        String endereco,

        @NotBlank
        @Size(max = 200)
        String ubsOrigem,

        Long cadastradoPorId
) {
}
