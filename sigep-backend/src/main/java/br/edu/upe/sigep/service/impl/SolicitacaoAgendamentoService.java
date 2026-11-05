package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.SolicitacaoAgendamento;
import br.edu.upe.sigep.enums.StatusSolicitacao;
import br.edu.upe.sigep.repository.SolicitacaoAgendamentoRepository;
import br.edu.upe.sigep.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public SolicitacaoAgendamento salvar(SolicitacaoAgendamento solicitacao) {
        if (solicitacao.getCriadoEm() == null) {
            solicitacao.setCriadoEm(LocalDateTime.now());
        }
        if (solicitacao.getStatusSolicitacao() == null) {
            solicitacao.setStatusSolicitacao(StatusSolicitacao.PENDENTE);
        }
        return solicitacaoAgendamentoRepository.save(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAgendamento> listarTodas() {
        return solicitacaoAgendamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<SolicitacaoAgendamento> buscarPorId(Long id) {
        return solicitacaoAgendamentoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAgendamento> buscarPorPaciente(Long pacienteId) {
        return solicitacaoAgendamentoRepository.findByPacienteId(pacienteId);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAgendamento> buscarPorStatus(StatusSolicitacao status) {
        return solicitacaoAgendamentoRepository.findByStatus(status);
    }

    @Transactional
    public SolicitacaoAgendamento aprovarSolicitacao(Long id, Long revisorId) {
        if (revisorId == null) {
            throw new IllegalArgumentException("O usuário revisor é obrigatório.");
        }

        SolicitacaoAgendamento solicitacao = solicitacaoAgendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada com ID: " + id));

        var revisor = usuarioRepository.findById(revisorId)
                .orElseThrow(() -> new RuntimeException("Usuário revisor não encontrado com ID: " + revisorId));

        if (solicitacao.getStatusSolicitacao() != StatusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Esta solicitação já foi analisada anteriormente.");
        }

        solicitacao.setAnalisadoPor(revisor);
        solicitacao.setRevisadoEm(LocalDateTime.now());
        solicitacao.setStatusSolicitacao(StatusSolicitacao.APROVADA);
        return solicitacaoAgendamentoRepository.save(solicitacao);
    }

    @Transactional
    public SolicitacaoAgendamento rejeitarSolicitacao(Long id, Long revisorId, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("O motivo da rejeição é obrigatório.");
        }
        if (revisorId == null) {
            throw new IllegalArgumentException("O usuário revisor é obrigatório.");
        }

        SolicitacaoAgendamento solicitacao = solicitacaoAgendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada com ID: " + id));

        var revisor = usuarioRepository.findById(revisorId)
                .orElseThrow(() -> new RuntimeException("Usuário revisor não encontrado com ID: " + revisorId));

        if (solicitacao.getStatusSolicitacao() != StatusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Esta solicitação já foi analisada anteriormente.");
        }

        solicitacao.setAnalisadoPor(revisor);
        solicitacao.setMotivoRejeicao(motivo);
        solicitacao.setRevisadoEm(LocalDateTime.now());
        solicitacao.setStatusSolicitacao(StatusSolicitacao.REJEITADA);
        return solicitacaoAgendamentoRepository.save(solicitacao);
    }
}
