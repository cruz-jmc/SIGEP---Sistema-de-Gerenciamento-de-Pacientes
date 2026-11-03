package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.EscalaAtendimento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EscalaService {

    private final EscalaAtendimentoRepository escalaAtendimentoRepository;

    @Transactional(readOnly = true)
    public List<EscalaAtendimento> listarEscalasAtivasPorProfissional(Long profissionalId) {
        return escalaAtendimentoRepository.findByProfissionalIdAndAtivoTrue(profissionalId);
    }

    @Transactional(readOnly = true)
    public EscalaAtendimento buscarEscalaPorId(Long escalaId) {
        return escalaAtendimentoRepository.findById(escalaId)
                .orElseThrow(() -> new IllegalArgumentException("Escala de atendimento não encontrada."));
    }
}