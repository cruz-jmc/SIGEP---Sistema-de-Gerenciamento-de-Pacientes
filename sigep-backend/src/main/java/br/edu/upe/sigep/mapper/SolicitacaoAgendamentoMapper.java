package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.request.SolicitacaoAgendamentoRequestDTO;
import br.edu.upe.sigep.dto.response.SolicitacaoAgendamentoResponseDTO;
import br.edu.upe.sigep.entity.*;
import org.springframework.stereotype.Component;

@Component
public class SolicitacaoAgendamentoMapper {

    public SolicitacaoAgendamento toEntity(
            SolicitacaoAgendamentoRequestDTO dto,
            Paciente paciente,
            Especialidade especialidade,
            Profissional profissional,
            Usuario cadastradoPor) {

        SolicitacaoAgendamento entity = new SolicitacaoAgendamento();
        entity.setGrupo(dto.getGrupo());
        entity.setDataAtendimentoInicial(dto.getDataAtendimentoInicial());
        entity.setPaciente(paciente);
        entity.setEspecialidade(especialidade);
        entity.setProfissional(profissional);
        entity.setCadastradoPor(cadastradoPor);
        return entity;
    }

    public SolicitacaoAgendamentoResponseDTO toResponseDTO(SolicitacaoAgendamento entity) {
        SolicitacaoAgendamentoResponseDTO dto = new SolicitacaoAgendamentoResponseDTO();
        dto.setId(entity.getId());
        dto.setGrupo(entity.getGrupo());
        dto.setDataAtendimentoInicial(entity.getDataAtendimentoInicial());
        dto.setStatusSolicitacao(entity.getStatusSolicitacao());
        dto.setMotivoRejeicao(entity.getMotivoRejeicao());
        dto.setCriadoEm(entity.getCriadoEm());
        dto.setRevisadoEm(entity.getRevisadoEm());

        if (entity.getPaciente() != null) {
            dto.setPacienteId(entity.getPaciente().getId());
            dto.setPacienteNome(entity.getPaciente().getNomeCompleto());
        }

        if (entity.getEspecialidade() != null) {
            dto.setEspecialidadeId(entity.getEspecialidade().getId());
            dto.setEspecialidadeNome(entity.getEspecialidade().getNome());
        }

        if (entity.getProfissional() != null) {
            dto.setProfissionalId(entity.getProfissional().getId());
            dto.setProfissionalNome(entity.getProfissional().getNome());
        }

        if (entity.getCadastradoPor() != null) {
            dto.setCadastradoPorId(entity.getCadastradoPor().getId());
        }

        if (entity.getAnalisadoPor() != null) {
            dto.setAnalisadoPorId(entity.getAnalisadoPor().getId());
        }

        return dto;
    }
}