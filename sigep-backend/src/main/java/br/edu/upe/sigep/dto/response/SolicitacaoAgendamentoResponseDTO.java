package br.edu.upe.sigep.dto.response;

import br.edu.upe.sigep.enums.Grupos;
import br.edu.upe.sigep.enums.StatusSolicitacao;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitacaoAgendamentoResponseDTO(
        Long id,
        Grupos grupo,
        LocalDate dataAtendimentoInicial,
        StatusSolicitacao statusSolicitacao,
        String motivoRejeicao,
        LocalDateTime criadoEm,
        LocalDateTime revisadoEm,
        Long pacienteId,
        String pacienteNome,
        Long especialidadeId,
        String especialidadeNome,
        Long profissionalId,
        String profissionalNome,
        Long cadastradoPorId,
        String cadastradoPorNome,
        Long analisadoPorId,
        String analisadoPorNome
) {
}