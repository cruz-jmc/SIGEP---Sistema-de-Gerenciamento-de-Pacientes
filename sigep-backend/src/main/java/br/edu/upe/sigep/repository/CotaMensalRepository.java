package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.CotaMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CotaMensalRepository extends JpaRepository<CotaMensal, Long> {
    List<CotaMensal> findByEspecialidadeIdAndMesRefernciaAndAnoReferencia(Long especialidadeid, LocalDate data);

}
