package br.edu.upe.sigep.chain.agendamento;

import br.edu.upe.sigep.dto.request.AgendamentoSessaoRequestDTO;
import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.entity.PlanoTratamento;

public record ValidacaoAgendamentoContext(
        AgendamentoSessaoRequestDTO dto,
        PlanoTratamento plano,
        EscalaAtendimento escala
) {
}