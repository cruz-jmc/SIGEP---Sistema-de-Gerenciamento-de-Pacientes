package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.request.EscalaAtendimentoRequestDTO;
import br.edu.upe.sigep.dto.response.EscalaAtendimentoResponseDTO;
import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.entity.Especialidade;
import br.edu.upe.sigep.entity.Profissional;
import org.springframework.stereotype.Component;

@Component
public class EscalaAtendimentoMapper {

    public EscalaAtendimento toEntity(EscalaAtendimentoRequestDTO dto, Profissional profissional, Especialidade especialidade) {
        EscalaAtendimento escala = new EscalaAtendimento();
        escala.setGrupo(dto.getGrupo());
        escala.setDiaSemana(dto.getDiaSemana());
        escala.setVigenciaInicio(dto.getVigenciaInicio());
        escala.setVigenciaFim(dto.getVigenciaFim());
        escala.setPacientesPorTurno(dto.getPacientesPorTurno());
        escala.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        escala.setProfissional(profissional);
        escala.setEspecialidade(especialidade);
        return escala;
    }

    public EscalaAtendimentoResponseDTO toResponseDTO(EscalaAtendimento escala) {
        EscalaAtendimentoResponseDTO dto = new EscalaAtendimentoResponseDTO();
        dto.setId(escala.getId());
        dto.setGrupo(escala.getGrupo());
        dto.setDiaSemana(escala.getDiaSemana());
        dto.setVigenciaInicio(escala.getVigenciaInicio());
        dto.setVigenciaFim(escala.getVigenciaFim());
        dto.setPacientesPorTurno(escala.getPacientesPorTurno());
        dto.setAtivo(escala.isAtivo());

        if (escala.getProfissional() != null) {
            dto.setProfissionalId(escala.getProfissional().getId());
            dto.setProfissionalNome(escala.getProfissional().getNome());
        }

        if (escala.getEspecialidade() != null) {
            dto.setEspecialidadeId(escala.getEspecialidade().getId());
            dto.setEspecialidadeNome(escala.getEspecialidade().getNome());
        }

        return dto;
    }
}