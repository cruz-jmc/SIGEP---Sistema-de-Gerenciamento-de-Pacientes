package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.request.EspecialidadeRequestDTO;
import br.edu.upe.sigep.dto.response.EspecialidadeResponseDTO;
import br.edu.upe.sigep.entity.Especialidade;

public final class EspecialidadeMapper {

    private EspecialidadeMapper() {
    }

    public static Especialidade toEntity(EspecialidadeRequestDTO dto) {
        Especialidade entidade = new Especialidade();
        entidade.setNome(dto.nome());
        entidade.setDescricao(dto.descricao());

        return entidade;
    }

    public static EspecialidadeResponseDTO toResponse(Especialidade entidade) {
        return new EspecialidadeResponseDTO(
                entidade.getId(),
                entidade.getNome(),
                entidade.getDescricao(),
                entidade.isAtivo());
    }
}
