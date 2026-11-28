package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.RealocacaoRequestDTO;
import br.edu.upe.sigep.dto.response.DataDisponivelRealocacaoResponseDTO;
import br.edu.upe.sigep.entity.AgendamentoSessao;
import br.edu.upe.sigep.entity.DataDisponivelRealocacao;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.CriterioPriorizacaoRealocacao;
import br.edu.upe.sigep.enums.StatusDataDisponivel;
import br.edu.upe.sigep.enums.StatusSessao;
import br.edu.upe.sigep.mapper.DataDisponivelRealocacaoMapper;
import br.edu.upe.sigep.repository.AgendamentoSessaoRepository;
import br.edu.upe.sigep.repository.DataDisponivelRealocacaoRepository;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.strategy.realocacao.PriorizacaoRealocacaoContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataDisponivelRealocacaoService {

    private final DataDisponivelRealocacaoRepository realocacaoRepository;
    private final AgendamentoSessaoRepository agendamentoSessaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DataDisponivelRealocacaoMapper mapper;
    private final PriorizacaoRealocacaoContext priorizacaoContext;

    @Transactional
    public DataDisponivelRealocacaoResponseDTO disponibilizarVaga(Long agendamentoOrigemId) {
        AgendamentoSessao sessaoCancelada = agendamentoSessaoRepository.findById(agendamentoOrigemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sessão de origem não encontrada com ID: " + agendamentoOrigemId));
        return disponibilizarVaga(sessaoCancelada);
    }

    @Transactional
    public DataDisponivelRealocacaoResponseDTO disponibilizarVaga(AgendamentoSessao sessaoCancelada) {
        if (sessaoCancelada == null || sessaoCancelada.getId() == null) {
            throw new IllegalArgumentException("A sessão cancelada deve estar persistida para liberar vaga.");
        }

        if (sessaoCancelada.getStatus() == StatusSessao.AGENDADA && sessaoCancelada.getCanceladoEm() == null) {
            throw new IllegalStateException("Apenas sessões canceladas podem disponibilizar vaga para realocação.");
        }

        if (realocacaoRepository.existsByAgendamentoOrigemId(sessaoCancelada.getId())) {
            throw new IllegalStateException("Já existe uma disponibilidade registrada para esta sessão de origem.");
        }

        Usuario usuarioLogado = obterUsuarioAutenticado();

        DataDisponivelRealocacao disponibilidade = new DataDisponivelRealocacao();
        disponibilidade.setDataDisponivel(sessaoCancelada.getDataAgendada());
        disponibilidade.setStatus(StatusDataDisponivel.DISPONIVEL);
        disponibilidade.setDisponibilizadaEm(LocalDateTime.now());
        disponibilidade.setEscalaAtendimento(sessaoCancelada.getEscalaAtendimento());
        disponibilidade.setPlanoOrigem(sessaoCancelada.getPlanoTratamento());
        disponibilidade.setAgendamentoOrigem(sessaoCancelada);
        disponibilidade.setCadastradoPor(usuarioLogado);

        return mapper.toResponseDTO(realocacaoRepository.save(disponibilidade));
    }

    @Transactional(readOnly = true)
    public List<DataDisponivelRealocacaoResponseDTO> listarDisponiveis() {
        return listarDisponiveis(CriterioPriorizacaoRealocacao.DATA_MAIS_PROXIMA);
    }

    @Transactional(readOnly = true)
    public List<DataDisponivelRealocacaoResponseDTO> listarDisponiveis(CriterioPriorizacaoRealocacao criterio) {
        List<DataDisponivelRealocacao> vagas = realocacaoRepository.findByStatus(StatusDataDisponivel.DISPONIVEL);
        return priorizacaoContext.executarPriorizacao(criterio, vagas)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DataDisponivelRealocacaoResponseDTO> sugerirMelhorVaga(CriterioPriorizacaoRealocacao criterio) {
        return listarDisponiveis(criterio).stream().findFirst();
    }

    @Transactional
    public DataDisponivelRealocacaoResponseDTO realocar(Long realocacaoId, RealocacaoRequestDTO dto) {
        DataDisponivelRealocacao disponibilidade = realocacaoRepository.buscarPorIdComLock(realocacaoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Disponibilidade de realocação não encontrada com ID: " + realocacaoId));

        if (disponibilidade.getStatus() != StatusDataDisponivel.DISPONIVEL) {
            throw new IllegalStateException(
                    "Esta vaga não está disponível para realocação (status atual: " + disponibilidade.getStatus() + ").");
        }

        AgendamentoSessao agendamentoDestino = agendamentoSessaoRepository.findById(dto.getAgendamentoDestinoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Agendamento de destino não encontrado com ID: " + dto.getAgendamentoDestinoId()));

        if (disponibilidade.getAgendamentoOrigem() != null
                && disponibilidade.getAgendamentoOrigem().getId().equals(agendamentoDestino.getId())) {
            throw new IllegalArgumentException("O agendamento de destino não pode ser o mesmo da origem.");
        }

        if (agendamentoDestino.getStatus() != StatusSessao.AGENDADA) {
            throw new IllegalStateException("Apenas sessões com status AGENDADA podem ser realocadas.");
        }

        if (!agendamentoDestino.getEscalaAtendimento().getId()
                .equals(disponibilidade.getEscalaAtendimento().getId())) {
            throw new IllegalArgumentException(
                    "O agendamento de destino deve pertencer à mesma escala de atendimento da vaga disponível.");
        }

        if (realocacaoRepository.existsByAgendamentoDestinoIdAndStatus(
                agendamentoDestino.getId(), StatusDataDisponivel.REALOCADA)) {
            throw new IllegalStateException("Este agendamento de destino já foi utilizado em outra realocação.");
        }

        agendamentoDestino.setDataAgendada(disponibilidade.getDataDisponivel());
        agendamentoSessaoRepository.save(agendamentoDestino);

        disponibilidade.setAgendamentoDestino(agendamentoDestino);
        disponibilidade.setStatus(StatusDataDisponivel.REALOCADA);
        disponibilidade.setRealocadaEm(LocalDateTime.now());

        return mapper.toResponseDTO(realocacaoRepository.save(disponibilidade));
    }

    private Usuario obterUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("Usuário não autenticado.");
        }
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário autenticado não encontrado: " + auth.getName()));
    }
}