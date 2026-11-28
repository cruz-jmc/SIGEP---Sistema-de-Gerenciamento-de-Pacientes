package br.edu.upe.sigep.chain.agendamento;

import br.edu.upe.sigep.repository.AgendamentoSessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConflitoHorarioHandler extends ValidadorAgendamentoHandler {

    private final AgendamentoSessaoRepository agendamentoSessaoRepository;

    @Override
    protected void executarValidacao(ValidacaoAgendamentoContext contexto) {
        Long planoId = contexto.plano().getId();

        boolean conflitoData = agendamentoSessaoRepository
                .existsByPlanoTratamentoIdAndDataAgendadaAndCanceladoEmIsNull(
                        planoId,
                        contexto.dto().dataAgendada());

        if (conflitoData) {
            throw new IllegalStateException(
                    "Já existe uma sessão agendada neste plano de tratamento para a data informada.");
        }

        boolean conflitoNumeroSessao = agendamentoSessaoRepository
                .existsByPlanoTratamentoIdAndNumeroSessaoAndCanceladoEmIsNull(
                        planoId,
                        contexto.dto().numeroSessao());

        if (conflitoNumeroSessao) {
            throw new IllegalStateException(
                    "Já existe uma sessão ativa com o número " + contexto.dto().numeroSessao() + " neste plano.");
        }
    }
}