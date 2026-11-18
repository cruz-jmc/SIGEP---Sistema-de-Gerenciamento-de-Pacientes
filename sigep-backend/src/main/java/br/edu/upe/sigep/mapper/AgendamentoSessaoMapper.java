package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.response.AgendamentoSessaoResponseDTO;
import br.edu.upe.sigep.entity.AgendamentoSessao;
import org.springframework.stereotype.Component;

@Component
public final class AgendamentoSessaoMapper {

    public AgendamentoSessaoResponseDTO toResponseDTO(AgendamentoSessao entity) {
        var plano = entity.getPlanoTratamento();
        var solicitacao = plano != null ? plano.getSolicitacao() : null;
        var paciente = solicitacao != null ? solicitacao.getPaciente() : null;
        var especialidade = solicitacao != null ? solicitacao.getEspecialidade() : null;
        var escala = entity.getEscalaAtendimento();
        var profissional = escala != null ? escala.getProfissional() : null;
        var registradoPor = entity.getPresencaRegistradaPor();

        return new AgendamentoSessaoResponseDTO(
                entity.getId(),
                entity.getNumeroSessao(),
                entity.getDataAgendada(),
                entity.getStatus(),
                entity.getStatusPresenca(),
                entity.getPresencaRegistradaEm(),
                entity.getCanceladoEm(),
                entity.getMotivoCancelamento(),
                entity.getCriadoEm(),
                plano != null ? plano.getId() : null,
                paciente != null ? paciente.getId() : null,
                paciente != null ? paciente.getNomeCompleto() : null,
                especialidade != null ? especialidade.getId() : null,
                especialidade != null ? especialidade.getNome() : null,
                escala != null ? escala.getId() : null,
                profissional != null ? profissional.getId() : null,
                profissional != null ? profissional.getNome() : null,
                registradoPor != null ? registradoPor.getId() : null,
                registradoPor != null ? registradoPor.getNome() : null
        );
    }
}