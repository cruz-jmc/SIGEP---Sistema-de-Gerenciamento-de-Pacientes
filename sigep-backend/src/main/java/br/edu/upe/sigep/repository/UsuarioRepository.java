package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    @Query("select u from Usuario u where lower(u.email) = lower(:email)")
    Optional<Usuario> buscarPorEmailNormalizado(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> buscarParaAlteracao(Long id);

    boolean existsByPerfil(PerfilUsuario perfil);
}
