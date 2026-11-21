
package br.edu.upe.sigep.service;

import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import java.util.List;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.entity.Usuario;

public interface UsuarioService {

    UsuarioResponseDTO buscarPorId(Long id);

    List<UsuarioResponseDTO> listarTodos();

    CadastroFuncionarioResultado cadastrarFuncionario(
            UsuarioCadastroRequestDTO dados,
            Usuario coordenador
    );

}
