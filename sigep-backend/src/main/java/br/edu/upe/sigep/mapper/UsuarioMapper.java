package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {

        if (usuario == null) {
            return null;
        }

        Long criadoPorId = usuario.getCriadoPor() != null
                ? usuario.getCriadoPor().getId()
                : null;
        /*//
        isso de cima é o mesmo que fazer:

        Long criadoPorId;

        if (usuario.getCriadoPor() != null) {
            criadoPorId = usuario.getCriadoPor().getId();
        } else {
            criadoPorId = null;
        }
        /*///

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.isPrimeiroAcesso(),
                criadoPorId,
                usuario.getCriadoEm()
        );
    }
}