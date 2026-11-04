package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.EscalaAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaAtendimentoRepository extends JpaRepository<EscalaAtendimento,Long> {
    List<EscalaAtendimento> findByProfissionalIdAndAtivoTrue(Long profissionalId);
}
