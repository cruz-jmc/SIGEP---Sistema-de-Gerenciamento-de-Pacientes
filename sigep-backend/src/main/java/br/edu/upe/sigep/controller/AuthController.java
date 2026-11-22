package br.edu.upe.sigep.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> usuarioAtual(
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of(
                        "email", authentication.getName(),
                        "autenticado", authentication.isAuthenticated(),
                        "permissoes", authentication.getAuthorities()
                                .stream()
                                .map(autoridade -> autoridade.getAuthority())
                                .toList()
                )
        );
    }
}