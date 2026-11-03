package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.CotaMensal;
import br.edu.upe.sigep.repository.CotaMensalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CotaMensalService {

    private final CotaMensalRepository cotaMensalRepository;

    @Transactional(readOnly = true)
    public boolean temCotaDisponivel(Long especialidadeId, LocalDate data) {
        return cotaMensalRepository.findByEspecialidadeIdAndMesReferenciaAndAnoReferencia(
                        especialidadeId, data.getMonthValue(), data.getYear())
                .map(cota -> cota.getQtdCotaLimite() > 0)
                .orElse(false);
    }

    @Transactional
    public void decrementarCota(Long especialidadeId, LocalDate data) {
        CotaMensal cota = cotaMensalRepository.findByEspecialidadeIdAndMesReferenciaAndAnoReferencia(
                        especialidadeId, data.getMonthValue(), data.getYear())
                .orElseThrow(() -> new IllegalArgumentException("Cota mensal não encontrada para esta especialidade e período."));

        if (cota.getQtdCotaLimite() <= 0) {
            throw new IllegalStateException("Não há vagas disponíveis para decrementar nesta cota.");
        }

        cota.setQtdCotaLimite(cota.getQtdCotaLimite() - 1);
        cotaMensalRepository.save(cota);
    }
}
