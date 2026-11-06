package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {
    Optional<Especialidade> findByNome(String nome);
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);
    List<Especialidade> findByAtivoTrue();
}
