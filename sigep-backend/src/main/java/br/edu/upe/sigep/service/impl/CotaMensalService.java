package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.CotaMensal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CotaMensalService {

    private final CotaMensalRepository cotaMensalRepository;

    @Transactional(readOnly = true)
    public boolean temCotaDisponivel(Long especialidadeId, LocalDate data){
        return cotaMensalRepository.findByEspecialidadeIdAnsMesAndAno(
                especialidadeId, data.getMonthValue(), data.getYear())
                .map(cota -> cota.getVagasDisponíveis() >0)
                .orElse(false);

    }

    @Transactional
    public void decrementarCota(Long especialidadeId, LocalDate data) {
        CotaMensal cota = cotaMensalRepository.findByEspecialidadeIdAndMesAndAno(
                        especialidadeId, data.getMonthValue(), data.getYear())
                .orElseThrow(() -> new IllegalArgumentException("Cota mensal não encontrada para esta especialidade e período."));

        if (cota.getVagasDisponiveis() <= 0) {
            throw new IllegalStateException("Não há vagas disponíveis para decrementar nesta cota.");
        }

        cota.setVagasDisponiveis(cota.getVagasDisponiveis() - 1);
        cotaMensalRepository.save(cota);


    }
}
