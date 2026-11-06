package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
    Optional<Profissional> findByRegistroConselho(String registroConselho);
    boolean existsByRegistroConselho(String registroConselho);
    boolean existsByRegistroConselhoAndIdNot(String registroConselho, Long id);
    List<Profissional> findByAtivoTrue();
}
