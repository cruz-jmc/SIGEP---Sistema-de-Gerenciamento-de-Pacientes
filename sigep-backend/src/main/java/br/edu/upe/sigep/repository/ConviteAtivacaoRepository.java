package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.ConviteAtivacao;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConviteAtivacaoRepository
        extends JpaRepository<ConviteAtivacao, Long> {

    Optional<ConviteAtivacao> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM ConviteAtivacao c
            WHERE c.tokenHash = :tokenHash
            """)
    Optional<ConviteAtivacao> buscarPorTokenHashComBloqueio(
            @Param("tokenHash") String tokenHash
    );
}