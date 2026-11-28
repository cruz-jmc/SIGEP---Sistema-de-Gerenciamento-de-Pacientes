package br.edu.upe.sigep.chain.agendamento;

import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.repository.AgendamentoSessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CapacidadeHandler extends ValidadorAgendamentoHandler {

    private final AgendamentoSessaoRepository agendamentoSessaoRepository;

    @Override
    protected void executarValidacao(ValidacaoAgendamentoContext contexto) {
        EscalaAtendimento escala = contexto.escala();
        long sessoesOcupadas = agendamentoSessaoRepository
                .countByEscalaAtendimentoIdAndDataAgendadaAndCanceladoEmIsNull(
                        escala.getId(),
                        contexto.dto().dataAgendada());

        if (sessoesOcupadas >= escala.getPacientesPorTurno()) {
            throw new IllegalStateException(
                    "Capacidade máxima por turno atingida para esta escala na data informada ("
                            + escala.getPacientesPorTurno() + " vagas).");
        }
    }
}