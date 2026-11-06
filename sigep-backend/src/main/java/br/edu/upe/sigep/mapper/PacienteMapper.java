package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.request.PacienteRequestDTO;
import br.edu.upe.sigep.dto.response.PacienteResponseDTO;
import br.edu.upe.sigep.entity.Paciente;
import br.edu.upe.sigep.entity.Usuario;

public final class PacienteMapper {

    private PacienteMapper() {
    }

    public static Paciente toEntity(PacienteRequestDTO dto) {
        Paciente entidade = new Paciente();
        entidade.setNomeCompleto(dto.nomeCompleto());
        entidade.setCpf(dto.cpf());
        entidade.setDataNascimento(dto.dataNascimento());
        entidade.setCartaoSus(dto.cartaoSus());
        entidade.setTelefone(dto.telefone());
        entidade.setEndereco(dto.endereco());
        entidade.setUbsOrigem(dto.ubsOrigem());

        if (dto.cadastradoPorId() != null) {
            Usuario referencia = new Usuario();
            referencia.setId(dto.cadastradoPorId());
            entidade.setCadastradoPor(referencia);
        }

        return entidade;
    }

    public static PacienteResponseDTO toResponse(Paciente entidade) {
        return new PacienteResponseDTO(
                entidade.getId(),
                entidade.getNomeCompleto(),
                entidade.getCpf(),
                entidade.getDataNascimento(),
                entidade.getCartaoSus(),
                entidade.getTelefone(),
                entidade.getEndereco(),
                entidade.getUbsOrigem(),
                entidade.isAtivo(),
                entidade.getCriadoEm(),
                entidade.getCadastradoPor() == null
                        ? null
                        : entidade.getCadastradoPor().getId());
    }
}
