package br.edu.upe.sigep.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponseDTO(
        Long id,
        String nomeCompleto,
        String cpf,
        LocalDate dataNascimento,
        String cartaoSus,
        String telefone,
        String endereco,
        String ubsOrigem,
        boolean ativo,
        LocalDateTime criadoEm,
        Long cadastradoPorId
) {
}
