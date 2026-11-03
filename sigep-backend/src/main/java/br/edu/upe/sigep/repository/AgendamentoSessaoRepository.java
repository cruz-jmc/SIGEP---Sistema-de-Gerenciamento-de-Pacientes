package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.AgendamentoSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoSessaoRepository extends JpaRepository<AgendamentoSessao, Long> {
    List<AgendamentoSessao> findByPlanoTratamentoIdOrderByNumeroSessaoAsc(Long planoId);
    long countByPlanoTratamentoIdAndStatusPresenca(Long planoId, statusPresenca statusPresenca);


}
