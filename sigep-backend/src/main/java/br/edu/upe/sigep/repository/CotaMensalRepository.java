package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.CotaMensal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CotaMensalRepository extends JpaRepository<CotaMensal, Long> {

    Optional<CotaMensal> findByProfissionalIdAndEspecialidadeIdAndMesReferenciaAndAnoReferencia(
            Long profissionalId, Long especialidadeId, Short mesReferencia, Short anoReferencia);

    boolean existsByProfissionalIdAndEspecialidadeIdAndMesReferenciaAndAnoReferencia(
            Long profissionalId, Long especialidadeId, Short mesReferencia, Short anoReferencia);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT c FROM CotaMensal c
           WHERE c.profissional.id = :profissionalId
             AND c.especialidade.id = :especialidadeId
             AND c.mesReferencia = :mes
             AND c.anoReferencia = :ano
           """)
    Optional<CotaMensal> buscarComLockParaAtualizacao(
            @Param("profissionalId") Long profissionalId,
            @Param("especialidadeId") Long especialidadeId,
            @Param("mes") Short mes,
            @Param("ano") Short ano);

    @Query("""
           SELECT c FROM CotaMensal c
           WHERE (:profissionalId IS NULL OR c.profissional.id = :profissionalId)
             AND (:especialidadeId IS NULL OR c.especialidade.id = :especialidadeId)
             AND (:mes IS NULL OR c.mesReferencia = :mes)
             AND (:ano IS NULL OR c.anoReferencia = :ano)
           """)
    List<CotaMensal> consultarPorFiltros(
            @Param("profissionalId") Long profissionalId,
            @Param("especialidadeId") Long especialidadeId,
            @Param("mes") Short mes,
            @Param("ano") Short ano);
}