package br.edu.upe.sigep.chain.agendamento;

public abstract class ValidadorAgendamentoHandler {

    private ValidadorAgendamentoHandler proximo;

    public ValidadorAgendamentoHandler definirProximo(ValidadorAgendamentoHandler proximo) {
        this.proximo = proximo;
        return proximo;
    }

    public final void validar(ValidacaoAgendamentoContext contexto) {
        executarValidacao(contexto);
        if (proximo != null) {
            proximo.validar(contexto);
        }
    }

    protected abstract void executarValidacao(ValidacaoAgendamentoContext contexto);
}