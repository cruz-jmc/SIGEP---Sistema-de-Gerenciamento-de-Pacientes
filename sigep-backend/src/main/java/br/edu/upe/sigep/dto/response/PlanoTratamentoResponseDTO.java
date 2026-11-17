package br.edu.upe.sigep.dto.response;

import br.edu.upe.sigep.enums.Grupos;
import br.edu.upe.sigep.enums.StatusTratamento;

import java.time.LocalDateTime;

public record PlanoTratamentoResponseDTO(
        Long id,
        StatusTratamento statusPlano,
        LocalDateTime aprovadoEm,
        LocalDateTime criadoEm,
        Long solicitacaoId,
        Grupos grupo,
        Long pacienteId,
        String pacienteNome,
        Long especialidadeId,
        String especialidadeNome,
        Long aprovadoPorId,
        String aprovadoPorNome
) {
}