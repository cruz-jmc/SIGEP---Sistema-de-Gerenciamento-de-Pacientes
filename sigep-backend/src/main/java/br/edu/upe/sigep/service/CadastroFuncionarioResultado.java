package br.edu.upe.sigep.service;

import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;

public record CadastroFuncionarioResultado(
        UsuarioResponseDTO usuario,
        String tokenConvite
) {}