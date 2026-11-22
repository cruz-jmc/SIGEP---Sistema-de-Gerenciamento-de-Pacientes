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
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                criadoPorId,
                usuario.getCriadoEm()
        );
    }
}