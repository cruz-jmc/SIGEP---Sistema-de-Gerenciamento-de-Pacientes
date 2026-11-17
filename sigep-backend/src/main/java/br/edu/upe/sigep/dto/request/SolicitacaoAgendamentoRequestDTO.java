package br.edu.upe.sigep.dto.request;

import br.edu.upe.sigep.enums.Grupos;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitacaoAgendamentoRequestDTO(
        @NotNull
        Grupos grupo,

        @NotNull
        LocalDate dataAtendimentoInicial,

        @NotNull
        Long pacienteId,

        @NotNull
        Long especialidadeId,

        Long profissionalId,

        @NotNull
        Long cadastradoPorId
) {
}