package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByCpfAndIdNot(String cpf, Long id);
    boolean existsByCartaoSus(String cartaoSus);
    boolean existsByCartaoSusAndIdNot(String cartaoSus, Long id);
    List<Paciente> findByAtivoTrue();
}
