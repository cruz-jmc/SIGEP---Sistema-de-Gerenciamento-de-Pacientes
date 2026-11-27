package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.EscalaAtendimentoRequestDTO;
import br.edu.upe.sigep.dto.response.EscalaAtendimentoResponseDTO;
import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.entity.Especialidade;
import br.edu.upe.sigep.entity.Profissional;
import br.edu.upe.sigep.enums.DiaSemana;
import br.edu.upe.sigep.mapper.EscalaAtendimentoMapper;
import br.edu.upe.sigep.repository.EscalaAtendimentoRepository;
import br.edu.upe.sigep.repository.EspecialidadeRepository;
import br.edu.upe.sigep.repository.ProfissionalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        validarRegrasEscala(dto);

        Profissional profissional = buscarProfissionalAtivo(dto.getProfissionalId());
        Especialidade especialidade = buscarEspecialidadeAtiva(dto.getEspecialidadeId());
        validarVinculoProfissionalEspecialidade(profissional, especialidade);

        EscalaAtendimento escala = mapper.toEntity(dto, profissional, especialidade);
        EscalaAtendimento salva = escalaAtendimentoRepository.save(escala);
        return mapper.toResponseDTO(salva);
    }

    @Transactional
    public EscalaAtendimentoResponseDTO atualizar(Long escalaId, EscalaAtendimentoRequestDTO dto) {
        validarRegrasEscala(dto);

        EscalaAtendimento existente = escalaAtendimentoRepository.findById(escalaId)
                .orElseThrow(() -> new EntityNotFoundException("Escala de atendimento não encontrada com ID: " + escalaId));

        Profissional profissional = buscarProfissionalAtivo(dto.getProfissionalId());
        Especialidade especialidade = buscarEspecialidadeAtiva(dto.getEspecialidadeId());
        validarVinculoProfissionalEspecialidade(profissional, especialidade);

        existente.setGrupo(dto.getGrupo());
        existente.setDiaSemana(dto.getDiaSemana());
        existente.setVigenciaInicio(dto.getVigenciaInicio());
        existente.setVigenciaFim(dto.getVigenciaFim());
        existente.setPacientesPorTurno(dto.getPacientesPorTurno());
        if (dto.getAtivo() != null) {
            existente.setAtivo(dto.getAtivo());
        }
        existente.setProfissional(profissional);
        existente.setEspecialidade(especialidade);

        return mapper.toResponseDTO(escalaAtendimentoRepository.save(existente));
    }

    @Transactional
    public void inativar(Long escalaId) {
        EscalaAtendimento existente = escalaAtendimentoRepository.findById(escalaId)
                .orElseThrow(() -> new EntityNotFoundException("Escala de atendimento não encontrada com ID: " + escalaId));

        existente.setAtivo(false);
        escalaAtendimentoRepository.save(existente);
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
                .orElseThrow(() -> new EntityNotFoundException("Escala de atendimento não encontrada com ID: " + escalaId));
        return mapper.toResponseDTO(escala);
    }

    @Transactional(readOnly = true)
    public EscalaAtendimento obterEscalaValidaParaData(Long profissionalId, Long especialidadeId, LocalDate data) {
        DiaSemana diaSemana = DiaSemana.fromLocalDate(data);
        return escalaAtendimentoRepository
                .buscarEscalaValida(profissionalId, especialidadeId, diaSemana, data)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não há escala vigente para o profissional, especialidade e data informados."));
    }

    private void validarRegrasEscala(EscalaAtendimentoRequestDTO dto) {
        if (dto.getVigenciaFim().isBefore(dto.getVigenciaInicio())) {
            throw new IllegalArgumentException("A data de fim da vigência não pode ser anterior à data de início.");
        }
        if (dto.getPacientesPorTurno() == null || dto.getPacientesPorTurno() <= 0) {
            throw new IllegalArgumentException("A capacidade de pacientes por turno deve ser maior que zero.");
        }
    }

    private Profissional buscarProfissionalAtivo(Long profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado com ID: " + profissionalId));
        if (!profissional.isAtivo()) {
            throw new IllegalStateException("Não é possível vincular escala a um profissional inativo.");
        }
        return profissional;
    }

    private Especialidade buscarEspecialidadeAtiva(Long especialidadeId) {
        Especialidade especialidade = especialidadeRepository.findById(especialidadeId)
                .orElseThrow(() -> new EntityNotFoundException("Especialidade não encontrada com ID: " + especialidadeId));
        if (!especialidade.isAtivo()) {
            throw new IllegalStateException("Não é possível vincular escala a uma especialidade inativa.");
        }
        return especialidade;
    }

    private void validarVinculoProfissionalEspecialidade(Profissional profissional, Especialidade especialidade) {
        if (profissional.getEspecialidade() == null
                || !profissional.getEspecialidade().getId().equals(especialidade.getId())) {
            throw new IllegalArgumentException("O profissional informado não pertence à especialidade selecionada.");
        }
    }
}