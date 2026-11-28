package br.edu.upe.sigep.strategy.realocacao;

import br.edu.upe.sigep.entity.DataDisponivelRealocacao;
import br.edu.upe.sigep.enums.CriterioPriorizacaoRealocacao;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class PriorizacaoPorDataMaisProximaStrategy implements PriorizacaoRealocacaoStrategy {

    @Override
    public CriterioPriorizacaoRealocacao getCriterio() {
        return CriterioPriorizacaoRealocacao.DATA_MAIS_PROXIMA;
    }

    @Override
    public List<DataDisponivelRealocacao> priorizar(List<DataDisponivelRealocacao> vagasDisponiveis) {
        return vagasDisponiveis.stream()
                .sorted(Comparator.comparing(DataDisponivelRealocacao::getDataDisponivel)
                        .thenComparing(
                                DataDisponivelRealocacao::getDisponibilizadaEm,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}