package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.PlanoTratamento;
import br.edu.upe.sigep.enums.statusTratamento;
import br.edu.upe.sigep.repository.PlanoTratamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanoTratamentoService {

    private final PlanoTratamentoRepository planoTratamentoRepository;

    @Transactional
    public PlanoTratamento salvar(PlanoTratamento planoTratamento) {
        if (planoTratamento.getCriadoEm() == null) {
            planoTratamento.setCriadoEm(LocalDateTime.now());
        }
        if (planoTratamento.getStatusPlano() == null) {
            planoTratamento.setStatusPlano(statusTratamento.ATIVO);
        }
        return planoTratamentoRepository.save(planoTratamento);
    }

    public List<PlanoTratamento> listarTodos() {
        return planoTratamentoRepository.findAll();
    }

    public Optional<PlanoTratamento> buscarPorId(Long id) {
        return planoTratamentoRepository.findById(id);
    }

    public Optional<PlanoTratamento> buscarPorSolicitacao(Long solicitacaoId) {
        return planoTratamentoRepository.findBySolicitacaoId(solicitacaoId);
    }

    public List<PlanoTratamento> buscarPorPacienteEStatus(Long pacienteId, statusTratamento statusPlano) {
        return planoTratamentoRepository.findBySolicitacaoPacienteIdAndStatusPlano(pacienteId, statusPlano);
    }

    @Transactional
    public PlanoTratamento concluirPlano(Long id) {
        PlanoTratamento plano = planoTratamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano de tratamento não encontrado com ID: " + id));

        plano.setStatusPlano(statusTratamento.CONCLUIDO);
        return planoTratamentoRepository.save(plano);
    }

    @Transactional
    public PlanoTratamento cancelarPlano(Long id) {
        PlanoTratamento plano = planoTratamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano de tratamento não encontrado com ID: " + id));

        plano.setStatusPlano(statusTratamento.CANCELADO_ABSENTEISMO);
        return planoTratamentoRepository.save(plano);
    }

    @Transactional
    public void deletarPorId(Long id) {
        planoTratamentoRepository.deleteById(id);
    }
}