
package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.mapper.UsuarioMapper;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.UsuarioService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public UsuarioResponseDTO buscarPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado: " + id
                        )
                );

        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {

        List<Usuario> usuarios = usuarioRepository.findAll();

        List<UsuarioResponseDTO> respostas = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            UsuarioResponseDTO dto = usuarioMapper.toResponseDTO(usuario);
            respostas.add(dto);
        }

        return respostas;
    }

}
