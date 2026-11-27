package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.response.DataDisponivelRealocacaoResponseDTO;
import br.edu.upe.sigep.entity.DataDisponivelRealocacao;
import org.springframework.stereotype.Component;

@Component
public class DataDisponivelRealocacaoMapper {

    public DataDisponivelRealocacaoResponseDTO toResponseDTO(DataDisponivelRealocacao entity) {
        Long escalaId = null;
        Long profissionalId = null;
        String profissionalNome = null;
        Long especialidadeId = null;
        String especialidadeNome = null;

        if (entity.getEscalaAtendimento() != null) {
            escalaId = entity.getEscalaAtendimento().getId();
            if (entity.getEscalaAtendimento().getProfissional() != null) {
                profissionalId = entity.getEscalaAtendimento().getProfissional().getId();
                profissionalNome = entity.getEscalaAtendimento().getProfissional().getNome();
            }
            if (entity.getEscalaAtendimento().getEspecialidade() != null) {
                especialidadeId = entity.getEscalaAtendimento().getEspecialidade().getId();
                especialidadeNome = entity.getEscalaAtendimento().getEspecialidade().getNome();
            }
        }

        return new DataDisponivelRealocacaoResponseDTO(
                entity.getId(),
                entity.getDataDisponivel(),
                entity.getStatus(),
                entity.getDisponibilizadaEm(),
                entity.getRealocadaEm(),
                escalaId,
                profissionalId,
                profissionalNome,
                especialidadeId,
                especialidadeNome,
                entity.getPlanoOrigem() != null ? entity.getPlanoOrigem().getId() : null,
                entity.getAgendamentoOrigem() != null ? entity.getAgendamentoOrigem().getId() : null,
                entity.getAgendamentoDestino() != null ? entity.getAgendamentoDestino().getId() : null,
                entity.getCadastradoPor() != null ? entity.getCadastradoPor().getId() : null
        );
    }
}