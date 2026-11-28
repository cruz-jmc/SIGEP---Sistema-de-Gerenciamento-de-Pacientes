package br.edu.upe.sigep.chain.agendamento;

import br.edu.upe.sigep.enums.StatusTratamento;
import org.springframework.stereotype.Component;

@Component
public class PlanoAtivoHandler extends ValidadorAgendamentoHandler {

    private static final short LIMITE_SESSOES_PLANO = 10;

    @Override
    protected void executarValidacao(ValidacaoAgendamentoContext contexto) {
        if (contexto.plano().getStatusPlano() != StatusTratamento.ATIVO) {
            throw new IllegalStateException("Só é possível agendar sessões para planos de tratamento ATIVOS.");
        }

        if (contexto.dto().numeroSessao() > LIMITE_SESSOES_PLANO) {
            throw new IllegalArgumentException(
                    "O número da sessão não pode ultrapassar o limite de " + LIMITE_SESSOES_PLANO + " sessões por plano.");
        }
    }
}