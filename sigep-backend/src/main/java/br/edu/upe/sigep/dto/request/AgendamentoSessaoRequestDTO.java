package br.edu.upe.sigep.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AgendamentoSessaoRequestDTO(
        @NotNull(message = "O ID do plano de tratamento é obrigatório.")
        Long planoTratamentoId,

        @NotNull(message = "O ID da escala de atendimento é obrigatório.")
        Long escalaAtendimentoId,

        @NotNull(message = "O número da sessão é obrigatório.")
        @Positive(message = "O número da sessão deve ser maior que zero.")
        Short numeroSessao,

        @NotNull(message = "A data agendada é obrigatória.")
        LocalDate dataAgendada
) {
}