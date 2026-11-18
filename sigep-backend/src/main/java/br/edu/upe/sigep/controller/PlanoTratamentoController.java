package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.response.PlanoTratamentoResponseDTO;
import br.edu.upe.sigep.enums.StatusTratamento;
import br.edu.upe.sigep.service.impl.PlanoTratamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planos-tratamento")
@RequiredArgsConstructor
public class PlanoTratamentoController {

    private final PlanoTratamentoService planoTratamentoService;

    @GetMapping
    public ResponseEntity<List<PlanoTratamentoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(planoTratamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoTratamentoResponseDTO> buscarPorId(
            @PathVariable Long id) {
        return planoTratamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/solicitacao/{solicitacaoId}")
    public ResponseEntity<PlanoTratamentoResponseDTO> buscarPorSolicitacao(
            @PathVariable Long solicitacaoId) {
        return planoTratamentoService.buscarPorSolicitacao(solicitacaoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{pacienteId}/status/{status}")
    public ResponseEntity<List<PlanoTratamentoResponseDTO>> buscarPorPacienteEStatus(
            @PathVariable Long pacienteId,
            @PathVariable StatusTratamento status) {
        return ResponseEntity.ok(
                planoTratamentoService.buscarPorPacienteEStatus(pacienteId, status));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<PlanoTratamentoResponseDTO> concluir(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                planoTratamentoService.concluirPlano(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PlanoTratamentoResponseDTO> cancelar(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                planoTratamentoService.cancelarPlano(id));
    }
}