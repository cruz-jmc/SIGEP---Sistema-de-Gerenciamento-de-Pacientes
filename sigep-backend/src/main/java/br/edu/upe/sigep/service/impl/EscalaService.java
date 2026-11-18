package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.EscalaAtendimentoRequestDTO;
import br.edu.upe.sigep.dto.response.EscalaAtendimentoResponseDTO;
import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.entity.Especialidade;
import br.edu.upe.sigep.entity.Profissional;
import br.edu.upe.sigep.mapper.EscalaAtendimentoMapper;
import br.edu.upe.sigep.repository.EscalaAtendimentoRepository;
import br.edu.upe.sigep.repository.EspecialidadeRepository;
import br.edu.upe.sigep.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EscalaService {

    private final EscalaAtendimentoRepository escalaAtendimentoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final EscalaAtendimentoMapper mapper;

    @Transactional
    public EscalaAtendimentoResponseDTO salvar(EscalaAtendimentoRequestDTO dto) {
        if (dto.getVigenciaFim().isBefore(dto.getVigenciaInicio())) {
            throw new IllegalArgumentException("A data de fim da vigência não pode ser anterior à data de início.");
        }

        Profissional profissional = profissionalRepository.findById(dto.getProfissionalId())
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado com ID: " + dto.getProfissionalId()));

        Especialidade especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com ID: " + dto.getEspecialidadeId()));

        EscalaAtendimento escala = mapper.toEntity(dto, profissional, especialidade);
        EscalaAtendimento salva = escalaAtendimentoRepository.save(escala);
        return mapper.toResponseDTO(salva);
    }

    @Transactional(readOnly = true)
    public List<EscalaAtendimentoResponseDTO> listarTodas() {
        return escalaAtendimentoRepository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EscalaAtendimentoResponseDTO> listarEscalasAtivasPorProfissional(Long profissionalId) {
        return escalaAtendimentoRepository.findByProfissionalIdAndAtivoTrue(profissionalId)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EscalaAtendimentoResponseDTO buscarEscalaPorId(Long escalaId) {
        EscalaAtendimento escala = escalaAtendimentoRepository.findById(escalaId)
                .orElseThrow(() -> new IllegalArgumentException("Escala de atendimento não encontrada com ID: " + escalaId));
        return mapper.toResponseDTO(escala);
    }
}