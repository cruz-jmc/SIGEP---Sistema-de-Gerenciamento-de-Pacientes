package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.response.PlanoTratamentoResponseDTO;
import br.edu.upe.sigep.entity.PlanoTratamento;
import org.springframework.stereotype.Component;

@Component
public final class PlanoTratamentoMapper {

    public PlanoTratamentoResponseDTO toResponseDTO(PlanoTratamento entity) {
        var solicitacao = entity.getSolicitacao();
        var paciente = solicitacao != null ? solicitacao.getPaciente() : null;
        var especialidade = solicitacao != null ? solicitacao.getEspecialidade() : null;
        var aprovadoPor = entity.getAprovadoPor();

        return new PlanoTratamentoResponseDTO(
                entity.getId(),
                entity.getStatusPlano(),
                entity.getAprovadoEm(),
                entity.getCriadoEm(),
                solicitacao != null ? solicitacao.getId() : null,
                solicitacao != null ? solicitacao.getGrupo() : null,
                paciente != null ? paciente.getId() : null,
                paciente != null ? paciente.getNomeCompleto() : null,
                especialidade != null ? especialidade.getId() : null,
                especialidade != null ? especialidade.getNome() : null,
                aprovadoPor != null ? aprovadoPor.getId() : null,
                aprovadoPor != null ? aprovadoPor.getNome() : null
        );
    }
}