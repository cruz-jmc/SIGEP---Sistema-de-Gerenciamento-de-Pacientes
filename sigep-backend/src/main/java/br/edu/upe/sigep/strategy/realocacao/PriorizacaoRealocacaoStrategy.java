package br.edu.upe.sigep.strategy.realocacao;

import br.edu.upe.sigep.entity.DataDisponivelRealocacao;
import br.edu.upe.sigep.enums.CriterioPriorizacaoRealocacao;

import java.util.List;

public interface PriorizacaoRealocacaoStrategy {

    CriterioPriorizacaoRealocacao getCriterio();

    List<DataDisponivelRealocacao> priorizar(List<DataDisponivelRealocacao> vagasDisponiveis);
}