package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.request.ProfissionalRequestDTO;
import br.edu.upe.sigep.dto.response.ProfissionalResponseDTO;
import br.edu.upe.sigep.entity.Profissional;
import br.edu.upe.sigep.entity.Usuario;

public final class ProfissionalMapper {

    private ProfissionalMapper() {
    }

    public static Profissional toEntity(ProfissionalRequestDTO dto) {
        Profissional entidade = new Profissional();
        entidade.setNome(dto.nome());
        entidade.setConselho(dto.conselho());
        entidade.setRegistroConselho(dto.registroConselho());

        if (dto.cadastradoPorId() != null) {
            Usuario referencia = new Usuario();
            referencia.setId(dto.cadastradoPorId());
            entidade.setCadastradoPor(referencia);
        }

        return entidade;
    }

    public static ProfissionalResponseDTO toResponse(Profissional entidade) {
        return new ProfissionalResponseDTO(
                entidade.getId(),
                entidade.getNome(),
                entidade.getConselho(),
                entidade.getRegistroConselho(),
                entidade.isAtivo(),
                entidade.getCriadoEm(),
                entidade.getCadastradoPor() == null
                        ? null
                        : entidade.getCadastradoPor().getId());
    }
}
