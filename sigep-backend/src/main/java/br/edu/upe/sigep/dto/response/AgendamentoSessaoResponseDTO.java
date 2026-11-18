package br.edu.upe.sigep.dto.response;

import br.edu.upe.sigep.enums.StatusPresenca;
import br.edu.upe.sigep.enums.StatusSessao;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AgendamentoSessaoResponseDTO(
        Long id,
        Short numeroSessao,
        LocalDate dataAgendada,
        StatusSessao status,
        StatusPresenca statusPresenca,
        LocalDateTime presencaRegistradaEm,
        LocalDateTime canceladoEm,
        String motivoCancelamento,
        LocalDateTime criadoEm,
        Long planoTratamentoId,
        Long pacienteId,
        String pacienteNome,
        Long especialidadeId,
        String especialidadeNome,
        Long escalaAtendimentoId,
        Long profissionalId,
        String profissionalNome,
        Long presencaRegistradaPorId,
        String presencaRegistradaPorNome
) {
}