package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.request.CoordenadorCredenciaisRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioAtualizacaoRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.service.UsuarioAutenticado;
import br.edu.upe.sigep.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();
    private final CsrfLogoutHandler csrfLogout;

    public UsuarioController(UsuarioService usuarioService, CookieCsrfTokenRepository csrfTokenRepository) {
        this.usuarioService = usuarioService;
        this.csrfLogout = new CsrfLogoutHandler(csrfTokenRepository);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> tratarJsonInvalido() {
        return Map.of("erro", "Corpo JSON inválido.");
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
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO cadastrarFuncionario(@Valid @RequestBody UsuarioCadastroRequestDTO dados,
                                                    @AuthenticationPrincipal UsuarioAutenticado coordenador) {
        return usuarioService.cadastrarFuncionario(dados, coordenador.id());
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizarFuncionario(@PathVariable Long id,
                                                   @Valid @RequestBody UsuarioAtualizacaoRequestDTO dados) {
        return usuarioService.atualizarFuncionario(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativarFuncionario(@PathVariable Long id) {
        usuarioService.desativarFuncionario(id);
    }

    @PutMapping("/coordenador/credenciais")
    public UsuarioResponseDTO atualizarCredenciais(@Valid @RequestBody CoordenadorCredenciaisRequestDTO dados,
                                                  @AuthenticationPrincipal UsuarioAutenticado coordenador,
                                                  Authentication autenticacao,
                                                  HttpServletRequest request, HttpServletResponse response) {
        UsuarioResponseDTO atualizado = usuarioService.atualizarCredenciais(coordenador.id(), dados);
        csrfLogout.logout(request, response, autenticacao);
        logout.logout(request, response, autenticacao);
        return atualizado;
    }
}
