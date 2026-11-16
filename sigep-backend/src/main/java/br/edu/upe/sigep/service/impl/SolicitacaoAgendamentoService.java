package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.SolicitacaoAgendamentoRequestDTO;
import br.edu.upe.sigep.dto.response.SolicitacaoAgendamentoResponseDTO;
import br.edu.upe.sigep.entity.*;
import br.edu.upe.sigep.enums.StatusSolicitacao;
import br.edu.upe.sigep.mapper.SolicitacaoAgendamentoMapper;
import br.edu.upe.sigep.repository.*;
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
    private final PacienteRepository pacienteRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitacaoAgendamentoMapper mapper;

    @Transactional
    public SolicitacaoAgendamentoResponseDTO salvar(SolicitacaoAgendamentoRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + dto.getPacienteId()));

        Especialidade especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com ID: " + dto.getEspecialidadeId()));

        Usuario cadastradoPor = usuarioRepository.findById(dto.getCadastradoPorId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + dto.getCadastradoPorId()));

        Profissional profissional = null;
        if (dto.getProfissionalId() != null) {
            profissional = profissionalRepository.findById(dto.getProfissionalId())
                    .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado com ID: " + dto.getProfissionalId()));
        }

        SolicitacaoAgendamento solicitacao = mapper.toEntity(dto, paciente, especialidade, profissional, cadastradoPor);
        solicitacao.setCriadoEm(LocalDateTime.now());
        solicitacao.setStatusSolicitacao(StatusSolicitacao.PENDENTE);

        SolicitacaoAgendamento salva = solicitacaoAgendamentoRepository.save(solicitacao);
        return mapper.toResponseDTO(salva);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAgendamentoResponseDTO> listarTodas() {
        return solicitacaoAgendamentoRepository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<SolicitacaoAgendamentoResponseDTO> buscarPorId(Long id) {
        return solicitacaoAgendamentoRepository.findById(id)
                .map(mapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAgendamentoResponseDTO> buscarPorPaciente(Long pacienteId) {
        return solicitacaoAgendamentoRepository.findByPacienteId(pacienteId)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAgendamentoResponseDTO> buscarPorStatus(StatusSolicitacao status) {
        return solicitacaoAgendamentoRepository.findByStatusSolicitacao(status)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAgendamentoResponseDTO> listarPendentes() {
        return solicitacaoAgendamentoRepository.findByStatusSolicitacao(StatusSolicitacao.PENDENTE)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public SolicitacaoAgendamentoResponseDTO aprovarSolicitacao(Long id, Long revisorId) {
        if (revisorId == null) {
            throw new IllegalArgumentException("O usuário revisor é obrigatório.");
        }

        SolicitacaoAgendamento solicitacao = solicitacaoAgendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com ID: " + id));

        var revisor = usuarioRepository.findById(revisorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário revisor não encontrado com ID: " + revisorId));

        if (solicitacao.getStatusSolicitacao() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Esta solicitação já foi analisada anteriormente.");
        }

        solicitacao.setAnalisadoPor(revisor);
        solicitacao.setRevisadoEm(LocalDateTime.now());
        solicitacao.setStatusSolicitacao(StatusSolicitacao.APROVADA);
        return mapper.toResponseDTO(solicitacaoAgendamentoRepository.save(solicitacao));
    }

    @Transactional
    public SolicitacaoAgendamentoResponseDTO rejeitarSolicitacao(Long id, Long revisorId, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("O motivo da rejeição é obrigatório.");
        }
        if (revisorId == null) {
            throw new IllegalArgumentException("O usuário revisor é obrigatório.");
        }

        SolicitacaoAgendamento solicitacao = solicitacaoAgendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com ID: " + id));

        var revisor = usuarioRepository.findById(revisorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário revisor não encontrado com ID: " + revisorId));

        if (solicitacao.getStatusSolicitacao() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Esta solicitação já foi analisada anteriormente.");
        }

        solicitacao.setAnalisadoPor(revisor);
        solicitacao.setMotivoRejeicao(motivo);
        solicitacao.setRevisadoEm(LocalDateTime.now());
        solicitacao.setStatusSolicitacao(StatusSolicitacao.REJEITADA);
        return mapper.toResponseDTO(solicitacaoAgendamentoRepository.save(solicitacao));
    }
}