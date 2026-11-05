package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.AgendamentoSessao;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.StatusPresenca;
import br.edu.upe.sigep.enums.StatusSessao;
import br.edu.upe.sigep.repository.AgendamentoSessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgendamentoSessaoService {

    private final AgendamentoSessaoRepository agendamentoSessaoRepository;

    @Transactional
    public AgendamentoSessao salvar(AgendamentoSessao agendamentoSessao) {
        if (agendamentoSessao.getCriadoEm() == null) {
            agendamentoSessao.setCriadoEm(LocalDateTime.now());
        }
        return agendamentoSessaoRepository.save(agendamentoSessao);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoSessao> listarTodos() {
        return agendamentoSessaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<AgendamentoSessao> buscarPorId(Long id) {
        return agendamentoSessaoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoSessao> buscarPorPlanoTratamentoOrdenado(Long planoId) {
        return agendamentoSessaoRepository.findByPlanoTratamentoIdOrderByNumeroSessaoAsc(planoId);
    }

    @Transactional(readOnly = true)
    public long contarPorPlanoEStatusPresenca(Long planoId, StatusPresenca status) {
        return agendamentoSessaoRepository.countByPlanoTratamentoIdAndStatusPresenca(planoId, status);
    }

    @Transactional
    public AgendamentoSessao registrarPresenca(Long id, StatusPresenca status, Usuario usuario) {
        AgendamentoSessao agendamento = agendamentoSessaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + id));

        agendamento.setStatusPresenca(status);
        agendamento.setPresencaRegistradaPor(usuario);
        agendamento.setPresencaRegistradaEm(LocalDateTime.now());

        return agendamentoSessaoRepository.save(agendamento);
    }

    @Transactional
    public AgendamentoSessao cancelarSessao(Long id, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("O motivo do cancelamento é obrigatório.");
        }

        AgendamentoSessao agendamento = agendamentoSessaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + id));

        agendamento.setStatus(StatusSessao.CANCELADA_ABSENTEISMO);
        agendamento.setMotivoCancelamento(motivo);
        agendamento.setCanceladoEm(LocalDateTime.now());

        return agendamentoSessaoRepository.save(agendamento);
    }

    @Transactional
    public void deletarPorId(Long id) {
        agendamentoSessaoRepository.deleteById(id);
    }
}
