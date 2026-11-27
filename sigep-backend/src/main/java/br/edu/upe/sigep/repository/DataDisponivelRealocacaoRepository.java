package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.DataDisponivelRealocacao;
import br.edu.upe.sigep.enums.StatusDataDisponivel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataDisponivelRealocacaoRepository extends JpaRepository<DataDisponivelRealocacao, Long> {

    List<DataDisponivelRealocacao> findByStatusOrderByDataDisponivelAsc(StatusDataDisponivel status);

    boolean existsByAgendamentoOrigemId(Long agendamentoOrigemId);

    boolean existsByAgendamentoDestinoIdAndStatus(Long agendamentoDestinoId, StatusDataDisponivel status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DataDisponivelRealocacao d WHERE d.id = :id")
    Optional<DataDisponivelRealocacao> buscarPorIdComLock(@Param("id") Long id);
}