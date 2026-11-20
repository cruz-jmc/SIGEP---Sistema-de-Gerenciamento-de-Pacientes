
package br.edu.upe.sigep.service;

import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO buscarPorId(Long id);

    List<UsuarioResponseDTO> listarTodos();

}
