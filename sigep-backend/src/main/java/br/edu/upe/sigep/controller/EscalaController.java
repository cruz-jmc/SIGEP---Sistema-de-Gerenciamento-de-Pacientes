package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.service.impl.EscalaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escalas")
@RequiredArgsConstructor
public class EscalaController {

    private final EscalaService escalaService;

    @GetMapping("/profissional/{profissionalId}/ativas")
    public ResponseEntity<List<EscalaAtendimento>> listarEscalasAtivasPorProfissional(
            @PathVariable Long profissionalId) {

        return ResponseEntity.ok(
                escalaService.listarEscalasAtivasPorProfissional(profissionalId));
    }

    @GetMapping("/{escalaId}")
    public ResponseEntity<EscalaAtendimento> buscarEscalaPorId(
            @PathVariable Long escalaId) {

        try {
            return ResponseEntity.ok(
                    escalaService.buscarEscalaPorId(escalaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
