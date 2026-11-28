package br.edu.upe.sigep.chain.agendamento;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidadorAgendamentoChain {

    private final PlanoAtivoHandler planoAtivoHandler;
    private final EscalaVigenteHandler escalaVigenteHandler;
    private final CapacidadeHandler capacidadeHandler;
    private final CotaDisponivelHandler cotaDisponivelHandler;
    private final ConflitoHorarioHandler conflitoHorarioHandler;

    @PostConstruct
    void montarCadeia() {
        planoAtivoHandler
                .definirProximo(escalaVigenteHandler)
                .definirProximo(capacidadeHandler)
                .definirProximo(cotaDisponivelHandler)
                .definirProximo(conflitoHorarioHandler);
    }

    public void validar(ValidacaoAgendamentoContext contexto) {
        planoAtivoHandler.validar(contexto);
    }
}