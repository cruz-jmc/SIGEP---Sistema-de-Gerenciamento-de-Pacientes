package br.edu.upe.sigep.chain.agendamento;

import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.service.impl.CotaMensalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CotaDisponivelHandler extends ValidadorAgendamentoHandler {

    private final CotaMensalService cotaMensalService;

    @Override
    protected void executarValidacao(ValidacaoAgendamentoContext contexto) {
        EscalaAtendimento escala = contexto.escala();
        boolean possuiCota = cotaMensalService.temCotaDisponivel(
                escala.getProfissional().getId(),
                escala.getEspecialidade().getId(),
                contexto.dto().dataAgendada());

        if (!possuiCota) {
            throw new IllegalStateException(
                    "Não há cota mensal disponível para o profissional e especialidade na data informada.");
        }
    }
}