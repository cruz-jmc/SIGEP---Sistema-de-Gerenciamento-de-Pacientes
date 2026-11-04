package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.SolicitacaoAgendamento;
import br.edu.upe.sigep.enums.statusSolicitacao;
import br.edu.upe.sigep.repository.SolicitacaoAgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SolicitacaoAgendamentoService {

    private final SolicitacaoAgendamentoRepository solicitacaoAgendamentoRepository;

    @Transactional
    public SolicitacaoAgendamento salvar(SolicitacaoAgendamento solicitacao) {
        if (solicitacao.getCriadoEm() == null) {
            solicitacao.setCriadoEm(LocalDateTime.now());
        }
        if (solicitacao.getStatusSolicitacao() == null) {
            solicitacao.setStatusSolicitacao(statusSolicitacao.PENDENTE);
        }
        return solicitacaoAgendamentoRepository.save(solicitacao);
    }

    public List<SolicitacaoAgendamento> listarTodas() {
        return solicitacaoAgendamentoRepository.findAll();
    }

    public Optional<SolicitacaoAgendamento> buscarPorId(Long id) {
        return solicitacaoAgendamentoRepository.findById(id);
    }

    public List<SolicitacaoAgendamento> buscarPorPaciente(Long pacienteId) {
        return solicitacaoAgendamentoRepository.findByPacienteId(pacienteId);
    }

    public List<SolicitacaoAgendamento> buscarPorStatus(statusSolicitacao status) {
        return solicitacaoAgendamentoRepository.findByStatus(status);
    }

    @Transactional
    public SolicitacaoAgendamento aprovarSolicitacao(Long id) {
        SolicitacaoAgendamento solicitacao = solicitacaoAgendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada com ID: " + id));

        if (solicitacao.getStatusSolicitacao() != statusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Esta solicitação já foi analisada anteriormente.");
        }

        solicitacao.setStatusSolicitacao(statusSolicitacao.APROVADA);
        return solicitacaoAgendamentoRepository.save(solicitacao);
    }

    @Transactional
    public SolicitacaoAgendamento rejeitarSolicitacao(Long id, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("O motivo da rejeição é obrigatório.");
        }

        SolicitacaoAgendamento solicitacao = solicitacaoAgendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada com ID: " + id));

        if (solicitacao.getStatusSolicitacao() != statusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Esta solicitação já foi analisada anteriormente.");
        }

        solicitacao.setStatusSolicitacao(statusSolicitacao.REJEITADA);
        solicitacao.setMotivoRejeicao(motivo);
        return solicitacaoAgendamentoRepository.save(solicitacao);
    }
}