package br.edu.upe.sigep.dto.response;

public record EspecialidadeResponseDTO(
        Long id,
        String nome,
        String descricao,
        boolean ativo
) {
}
