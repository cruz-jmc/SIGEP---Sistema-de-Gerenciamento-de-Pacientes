package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.NotificacaoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoUsuarioRepository extends JpaRepository<NotificacaoUsuario, Long> {
    List<NotificacaoUsuario> findByDestinatarioIdOrderByCriadaEmDesc(Long destinatarioId);
    List<NotificacaoUsuario> findByDestinatarioIdAndLidaFalse(Long destinatarioId);
}
