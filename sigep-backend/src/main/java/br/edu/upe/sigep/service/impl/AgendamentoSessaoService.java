package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.chain.agendamento.ValidacaoAgendamentoContext;
import br.edu.upe.sigep.chain.agendamento.ValidadorAgendamentoChain;
import br.edu.upe.sigep.dto.request.AgendamentoSessaoRequestDTO;
import br.edu.upe.sigep.dto.response.AgendamentoSessaoResponseDTO;
import br.edu.upe.sigep.entity.AgendamentoSessao;
import br.edu.upe.sigep.enums.StatusPresenca;
import br.edu.upe.sigep.enums.StatusSessao;
import br.edu.upe.sigep.mapper.AgendamentoSessaoMapper;
import br.edu.upe.sigep.repository.AgendamentoSessaoRepository;
import br.edu.upe.sigep.repository.EscalaAtendimentoRepository;
import br.edu.upe.sigep.repository.PlanoTratamentoRepository;
import br.edu.upe.sigep.repository.UsuarioRepository;
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
    private final PlanoTratamentoRepository planoTratamentoRepository;
    private final EscalaAtendimentoRepository escalaAtendimentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendamentoSessaoMapper mapper;
    private final ValidadorAgendamentoChain validadorAgendamentoChain;
    private final CotaMensalService cotaMensalService;

    @Transactional
    public AgendamentoSessaoResponseDTO salvar(AgendamentoSessaoRequestDTO dto) {
        var plano = planoTratamentoRepository.findById(dto.planoTratamentoId())
                .orElseThrow(() -> new RuntimeException("Plano de tratamento não encontrado com ID: " + dto.planoTratamentoId()));

        var escala = escalaAtendimentoRepository.findById(dto.escalaAtendimentoId())
                .orElseThrow(() -> new RuntimeException("Escala de atendimento não encontrada com ID: " + dto.escalaAtendimentoId()));

        validadorAgendamentoChain.validar(new ValidacaoAgendamentoContext(dto, plano, escala));

        AgendamentoSessao agendamento = new AgendamentoSessao();
        agendamento.setPlanoTratamento(plano);
        agendamento.setEscalaAtendimento(escala);
        agendamento.setNumeroSessao(dto.numeroSessao());
        agendamento.setDataAgendada(dto.dataAgendada());
        agendamento.setStatus(StatusSessao.AGENDADA);
        agendamento.setStatusPresenca(StatusPresenca.PENDENTE);
        agendamento.setCriadoEm(LocalDateTime.now());

        AgendamentoSessao salvo = agendamentoSessaoRepository.save(agendamento);

        cotaMensalService.decrementarCota(
                escala.getProfissional().getId(),
                escala.getEspecialidade().getId(),
                dto.dataAgendada());

        return mapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoSessaoResponseDTO> listarTodos() {
        return agendamentoSessaoRepository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AgendamentoSessaoResponseDTO> buscarPorId(Long id) {
        return agendamentoSessaoRepository.findById(id)
                .map(mapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoSessaoResponseDTO> buscarPorPlanoTratamentoOrdenado(Long planoId) {
        return agendamentoSessaoRepository.findByPlanoTratamentoIdOrderByNumeroSessaoAsc(planoId)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarPorPlanoEStatusPresenca(Long planoId, StatusPresenca status) {
        return agendamentoSessaoRepository.countByPlanoTratamentoIdAndStatusPresenca(planoId, status);
    }

    @Transactional
    public AgendamentoSessaoResponseDTO registrarPresenca(Long id, StatusPresenca status, Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("O ID do usuário que registrou a presença é obrigatório.");
        }

        AgendamentoSessao agendamento = agendamentoSessaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + id));

        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + usuarioId));

        agendamento.setStatusPresenca(status);
        agendamento.setPresencaRegistradaPor(usuario);
        agendamento.setPresencaRegistradaEm(LocalDateTime.now());

        return mapper.toResponseDTO(agendamentoSessaoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoSessaoResponseDTO cancelarSessao(Long id, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("O motivo do cancelamento é obrigatório.");
        }

        AgendamentoSessao agendamento = agendamentoSessaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + id));

        agendamento.setStatus(StatusSessao.CANCELADA_ABSENTEISMO);
        agendamento.setMotivoCancelamento(motivo);
        agendamento.setCanceladoEm(LocalDateTime.now());

        return mapper.toResponseDTO(agendamentoSessaoRepository.save(agendamento));
    }

    @Transactional
    public void deletarPorId(Long id) {
        agendamentoSessaoRepository.deleteById(id);
    }
}