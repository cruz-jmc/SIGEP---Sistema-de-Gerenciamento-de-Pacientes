package br.edu.upe.sigep.service;

import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String emailNormalizado =
                email.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByEmail(emailNormalizado)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Credenciais inválidas."
                        )
                );

        if (usuario.getSenhaHash() == null) {
            throw new UsernameNotFoundException(
                    "Credenciais inválidas."
            );
        }

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities(
                        "ROLE_" + usuario.getPerfil().name()
                )
                .disabled(!usuario.isAtivo())
                .build();
    }
}