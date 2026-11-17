package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.response.SolicitacaoAgendamentoResponseDTO;
import br.edu.upe.sigep.entity.SolicitacaoAgendamento;
import org.springframework.stereotype.Component;

@Component
public final class SolicitacaoAgendamentoMapper {

    public SolicitacaoAgendamentoResponseDTO toResponseDTO(SolicitacaoAgendamento entity) {
        return new SolicitacaoAgendamentoResponseDTO(
                entity.getId(),
                entity.getGrupo(),
                entity.getDataAtendimentoInicial(),
                entity.getStatusSolicitacao(),
                entity.getMotivoRejeicao(),
                entity.getCriadoEm(),
                entity.getRevisadoEm(),
                entity.getPaciente() != null ? entity.getPaciente().getId() : null,
                entity.getPaciente() != null ? entity.getPaciente().getNomeCompleto() : null,
                entity.getEspecialidade() != null ? entity.getEspecialidade().getId() : null,
                entity.getEspecialidade() != null ? entity.getEspecialidade().getNome() : null,
                entity.getProfissional() != null ? entity.getProfissional().getId() : null,
                entity.getProfissional() != null ? entity.getProfissional().getNome() : null,
                entity.getCadastradoPor() != null ? entity.getCadastradoPor().getId() : null,
                entity.getCadastradoPor() != null ? entity.getCadastradoPor().getNome() : null,
                entity.getAnalisadoPor() != null ? entity.getAnalisadoPor().getId() : null,
                entity.getAnalisadoPor() != null ? entity.getAnalisadoPor().getNome() : null
        );
    }
}