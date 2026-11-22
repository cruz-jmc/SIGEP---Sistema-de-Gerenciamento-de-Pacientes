package br.edu.upe.sigep.service;

import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public final class UsuarioAutenticado extends User {

    private final Long id;
    private final PerfilUsuario perfil;
    private final long revisaoCredenciais;

    public UsuarioAutenticado(Usuario usuario) {
        super(usuario.getEmail(), usuario.getSenhaHash(), usuario.isAtivo(), true, true, true,
                AuthorityUtils.createAuthorityList("ROLE_" + usuario.getPerfil().name()));
        this.id = usuario.getId();
        this.perfil = usuario.getPerfil();
        this.revisaoCredenciais = usuario.getRevisaoCredenciais();
    }

    public Long id() {
        return id;
    }

    public boolean sessaoValida(Usuario usuario) {
        return usuario.isAtivo() && perfil == usuario.getPerfil()
                && getUsername().equals(usuario.getEmail())
                && revisaoCredenciais == usuario.getRevisaoCredenciais();
    }
}
