package br.edu.upe.sigep.service;

import br.edu.upe.sigep.dto.request.CoordenadorCredenciaisRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioAtualizacaoRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO buscarPorId(Long id);

    List<UsuarioResponseDTO> listarTodos();

    UsuarioResponseDTO cadastrarFuncionario(UsuarioCadastroRequestDTO dados, Long coordenadorId);

    UsuarioResponseDTO atualizarFuncionario(Long id, UsuarioAtualizacaoRequestDTO dados);

    void desativarFuncionario(Long id);

    UsuarioResponseDTO atualizarCredenciais(Long coordenadorId, CoordenadorCredenciaisRequestDTO dados);
}
