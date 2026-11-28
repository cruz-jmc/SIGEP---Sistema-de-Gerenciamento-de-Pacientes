package br.edu.upe.sigep.strategy.realocacao;

import br.edu.upe.sigep.entity.DataDisponivelRealocacao;
import br.edu.upe.sigep.enums.CriterioPriorizacaoRealocacao;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class PriorizacaoPorOrdemDisponibilizacaoStrategy implements PriorizacaoRealocacaoStrategy {

    @Override
    public CriterioPriorizacaoRealocacao getCriterio() {
        return CriterioPriorizacaoRealocacao.ORDEM_DISPONIBILIZACAO;
    }

    @Override
    public List<DataDisponivelRealocacao> priorizar(List<DataDisponivelRealocacao> vagasDisponiveis) {
        return vagasDisponiveis.stream()
                .sorted(Comparator.comparing(
                                DataDisponivelRealocacao::getDisponibilizadaEm,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(DataDisponivelRealocacao::getDataDisponivel))
                .toList();
    }
}