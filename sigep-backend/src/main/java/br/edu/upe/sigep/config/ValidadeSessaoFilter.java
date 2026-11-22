package br.edu.upe.sigep.config;

import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.UsuarioAutenticado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

final class ValidadeSessaoFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarios;
    private final SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();

    ValidadeSessaoFilter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof UsuarioAutenticado usuario
                && usuarios.findById(usuario.id()).filter(usuario::sessaoValida).isEmpty()) {
            logout.logout(request, response, autenticacao);
        }
        chain.doFilter(request, response);
    }
}
