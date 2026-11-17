package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.response.PlanoTratamentoResponseDTO;
import br.edu.upe.sigep.entity.PlanoTratamento;
import br.edu.upe.sigep.enums.StatusTratamento;
import br.edu.upe.sigep.mapper.PlanoTratamentoMapper;
import br.edu.upe.sigep.repository.PlanoTratamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanoTratamentoService {

    private final PlanoTratamentoRepository planoTratamentoRepository;
    private final PlanoTratamentoMapper mapper;

    @Transactional(readOnly = true)
    public List<PlanoTratamentoResponseDTO> listarTodos() {
        return planoTratamentoRepository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PlanoTratamentoResponseDTO> buscarPorId(Long id) {
        return planoTratamentoRepository.findById(id)
                .map(mapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<PlanoTratamentoResponseDTO> buscarPorSolicitacao(Long solicitacaoId) {
        return planoTratamentoRepository.findBySolicitacaoId(solicitacaoId)
                .map(mapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<PlanoTratamentoResponseDTO> buscarPorPacienteEStatus(Long pacienteId, StatusTratamento statusPlano) {
        return planoTratamentoRepository.findBySolicitacaoPacienteIdAndStatusPlano(pacienteId, statusPlano)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public PlanoTratamentoResponseDTO concluirPlano(Long id) {
        PlanoTratamento plano = planoTratamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano de tratamento não encontrado com ID: " + id));

        plano.setStatusPlano(StatusTratamento.CONCLUIDO);
        return mapper.toResponseDTO(planoTratamentoRepository.save(plano));
    }

    @Transactional
    public PlanoTratamentoResponseDTO cancelarPlano(Long id) {
        PlanoTratamento plano = planoTratamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano de tratamento não encontrado com ID: " + id));

        plano.setStatusPlano(StatusTratamento.CANCELADO_ABSENTEISMO);
        return mapper.toResponseDTO(planoTratamentoRepository.save(plano));
    }
}