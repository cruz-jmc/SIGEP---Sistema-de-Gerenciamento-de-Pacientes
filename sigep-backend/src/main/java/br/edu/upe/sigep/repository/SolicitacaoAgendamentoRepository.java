package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.SolicitacaoAgendamento;
import br.edu.upe.sigep.enums.statusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoAgendamentoRepository extends JpaRepository<SolicitacaoAgendamento, Long> {

    List<SolicitacaoAgendamento> findByPacienteId(Long pacienteId);

    List<SolicitacaoAgendamento> findByStatus(statusSolicitacao status);
}