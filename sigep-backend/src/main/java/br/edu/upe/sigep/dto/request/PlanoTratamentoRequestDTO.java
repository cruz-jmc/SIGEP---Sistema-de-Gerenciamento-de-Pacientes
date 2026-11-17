package br.edu.upe.sigep.dto.request;

import jakarta.validation.constraints.NotNull;

public record PlanoTratamentoRequestDTO(
        @NotNull
        Long solicitacaoId,

        @NotNull
        Long aprovadoPorId
) {
}