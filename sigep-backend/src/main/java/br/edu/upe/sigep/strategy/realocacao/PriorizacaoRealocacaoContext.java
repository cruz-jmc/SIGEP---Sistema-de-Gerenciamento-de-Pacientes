package br.edu.upe.sigep.strategy.realocacao;

import br.edu.upe.sigep.entity.DataDisponivelRealocacao;
import br.edu.upe.sigep.enums.CriterioPriorizacaoRealocacao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PriorizacaoRealocacaoContext {

    private final Map<CriterioPriorizacaoRealocacao, PriorizacaoRealocacaoStrategy> estrategias;

    public PriorizacaoRealocacaoContext(List<PriorizacaoRealocacaoStrategy> listaEstrategias) {
        this.estrategias = listaEstrategias.stream()
                .collect(Collectors.toUnmodifiableMap(
                        PriorizacaoRealocacaoStrategy::getCriterio,
                        Function.identity()
                ));
    }

    public List<DataDisponivelRealocacao> executarPriorizacao(
            CriterioPriorizacaoRealocacao criterio,
            List<DataDisponivelRealocacao> vagasDisponiveis) {

        CriterioPriorizacaoRealocacao criterioEfetivo =
                (criterio != null) ? criterio : CriterioPriorizacaoRealocacao.DATA_MAIS_PROXIMA;

        PriorizacaoRealocacaoStrategy estrategia = estrategias.get(criterioEfetivo);
        if (estrategia == null) {
            throw new IllegalArgumentException("Estratégia de priorização não suportada: " + criterioEfetivo);
        }

        return estrategia.priorizar(vagasDisponiveis);
    }
}