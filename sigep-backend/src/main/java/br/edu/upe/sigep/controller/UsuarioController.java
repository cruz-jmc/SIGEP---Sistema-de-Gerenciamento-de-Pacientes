package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.CadastroFuncionarioResultado;
import br.edu.upe.sigep.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository) {

        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @GetMapping
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrarFuncionario(
            @Valid @RequestBody UsuarioCadastroRequestDTO dados,
            Authentication authentication) {

        String emailCoordenador = authentication.getName();

        Usuario coordenador = usuarioRepository
                .findByEmail(emailCoordenador)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Coordenador não encontrado."
                        )
                );

        CadastroFuncionarioResultado resultado =
                usuarioService.cadastrarFuncionario(
                        dados,
                        coordenador
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultado.usuario());
    }
}