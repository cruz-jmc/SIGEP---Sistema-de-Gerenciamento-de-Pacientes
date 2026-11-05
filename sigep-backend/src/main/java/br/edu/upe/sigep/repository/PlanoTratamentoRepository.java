package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.PlanoTratamento;
import br.edu.upe.sigep.enums.StatusTratamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanoTratamentoRepository extends JpaRepository<PlanoTratamento, Long> {

    Optional<PlanoTratamento> findBySolicitacaoId(Long solicitacaoId);
    List<PlanoTratamento> findBySolicitacaoPacienteIdAndStatusPlano(Long pacienteId, StatusTratamento statusPlano);



}
