package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.AgendamentoSessao;
import br.edu.upe.sigep.enums.StatusPresenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoSessaoRepository extends JpaRepository<AgendamentoSessao, Long> {
    List<AgendamentoSessao> findByPlanoTratamentoIdOrderByNumeroSessaoAsc(Long planoId);
    long countByPlanoTratamentoIdAndStatusPresenca(Long planoId, StatusPresenca statusPresenca);

    long countByEscalaAtendimentoIdAndDataAgendadaAndCanceladoEmIsNull(Long escalaAtendimentoId, LocalDate dataAgendada);
    boolean existsByPlanoTratamentoIdAndDataAgendadaAndCanceladoEmIsNull(Long planoTratamentoId, LocalDate dataAgendada);
    boolean existsByPlanoTratamentoIdAndNumeroSessaoAndCanceladoEmIsNull(Long planoTratamentoId, Short numeroSessao);
}