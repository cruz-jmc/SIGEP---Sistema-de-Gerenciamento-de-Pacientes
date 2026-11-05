package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.DataDisponivelRealocacao;
import br.edu.upe.sigep.enums.StatusDataDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataDisponivelRealocacaoRepository  extends JpaRepository<DataDisponivelRealocacao, Long> {
    List<DataDisponivelRealocacao> findByStatus(StatusDataDisponivel status);
}
