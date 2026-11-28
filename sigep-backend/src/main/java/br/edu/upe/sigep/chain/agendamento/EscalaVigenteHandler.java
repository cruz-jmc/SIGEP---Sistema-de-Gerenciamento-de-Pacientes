package br.edu.upe.sigep.chain.agendamento;

import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.enums.DiaSemana;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EscalaVigenteHandler extends ValidadorAgendamentoHandler {

    @Override
    protected void executarValidacao(ValidacaoAgendamentoContext contexto) {
        EscalaAtendimento escala = contexto.escala();
        LocalDate dataAgendada = contexto.dto().dataAgendada();

        if (!escala.isAtivo()) {
            throw new IllegalStateException("A escala de atendimento selecionada está inativa.");
        }

        if (escala.getProfissional() == null || !escala.getProfissional().isAtivo()) {
            throw new IllegalStateException("O profissional vinculado à escala está inativo.");
        }

        if (escala.getEspecialidade() == null || !escala.getEspecialidade().isAtivo()) {
            throw new IllegalStateException("A especialidade vinculada à escala está inativa.");
        }

        if (dataAgendada.isBefore(escala.getVigenciaInicio()) || dataAgendada.isAfter(escala.getVigenciaFim())) {
            throw new IllegalArgumentException("A data agendada está fora do período de vigência da escala.");
        }

        DiaSemana diaData = DiaSemana.fromLocalDate(dataAgendada);
        if (diaData != escala.getDiaSemana()) {
            throw new IllegalArgumentException(
                    "A data agendada (" + diaData + ") não corresponde ao dia da escala (" + escala.getDiaSemana() + ").");
        }
    }
}