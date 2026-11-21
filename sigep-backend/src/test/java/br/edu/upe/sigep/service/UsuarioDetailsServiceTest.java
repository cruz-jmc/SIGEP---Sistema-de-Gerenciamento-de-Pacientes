package br.edu.upe.sigep.service;

import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioDetailsServiceTest {

    private static final String EMAIL = "pessoa@sigep.example";
    private static final String HASH = "hash-ficticio";

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final UsuarioDetailsService service = new UsuarioDetailsService(repository);

    @ParameterizedTest
    @EnumSource(PerfilUsuario.class)
    void deveNormalizarEmailECarregarAutoridade(PerfilUsuario perfil) {
        configurarUsuario(perfil, HASH, true);

        UserDetails detalhes = service.loadUserByUsername("  PESSOA@SIGEP.EXAMPLE  ");

        assertEquals(EMAIL, detalhes.getUsername());
        assertEquals(HASH, detalhes.getPassword());
        assertTrue(detalhes.isEnabled());
        assertEquals(List.of("ROLE_" + perfil.name()), detalhes.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
    }

    @Test
    void deveRejeitarUsuarioInexistente() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(EMAIL));
    }

    @Test
    void deveRejeitarUsuarioSemHashDeSenha() {
        configurarUsuario(PerfilUsuario.FUNCIONARIO, null, false);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(EMAIL));
    }

    @Test
    void deveMarcarUsuarioInativoComSenhaComoDesabilitado() {
        configurarUsuario(PerfilUsuario.FUNCIONARIO, HASH, false);

        assertFalse(service.loadUserByUsername(EMAIL).isEnabled());
    }

    private void configurarUsuario(PerfilUsuario perfil, String hash, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setEmail(EMAIL);
        usuario.setPerfil(perfil);
        usuario.setSenhaHash(hash);
        usuario.setAtivo(ativo);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
    }
}
