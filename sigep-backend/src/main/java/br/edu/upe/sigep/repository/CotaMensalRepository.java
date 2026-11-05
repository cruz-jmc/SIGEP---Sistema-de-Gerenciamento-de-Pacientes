package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.CotaMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CotaMensalRepository extends JpaRepository<CotaMensal, Long> {
    Optional<CotaMensal> findByEspecialidadeIdAndMesReferenciaAndAnoReferencia(
            Long especialidadeId, Short mesReferencia, Short anoReferencia);
}
