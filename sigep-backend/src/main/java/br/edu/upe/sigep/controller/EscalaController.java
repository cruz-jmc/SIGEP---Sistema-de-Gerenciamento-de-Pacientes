package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.request.EscalaAtendimentoRequestDTO;
import br.edu.upe.sigep.dto.response.EscalaAtendimentoResponseDTO;
import br.edu.upe.sigep.service.impl.EscalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escalas")
@RequiredArgsConstructor
public class EscalaController {

    private final EscalaService escalaService;

    @PostMapping
    public ResponseEntity<EscalaAtendimentoResponseDTO> salvar(
            @Valid @RequestBody EscalaAtendimentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(escalaService.salvar(dto));
    }

    @GetMapping
    public ResponseEntity<List<EscalaAtendimentoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(escalaService.listarTodas());
    }

    @GetMapping("/profissional/{profissionalId}/ativas")
    public ResponseEntity<List<EscalaAtendimentoResponseDTO>> listarEscalasAtivasPorProfissional(
            @PathVariable Long profissionalId) {
        return ResponseEntity.ok(
                escalaService.listarEscalasAtivasPorProfissional(profissionalId));
    }

    @GetMapping("/{escalaId}")
    public ResponseEntity<EscalaAtendimentoResponseDTO> buscarEscalaPorId(
            @PathVariable Long escalaId) {
        try {
            return ResponseEntity.ok(
                    escalaService.buscarEscalaPorId(escalaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}