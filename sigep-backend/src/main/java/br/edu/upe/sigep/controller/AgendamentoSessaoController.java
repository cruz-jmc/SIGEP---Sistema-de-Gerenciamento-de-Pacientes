package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.request.AgendamentoSessaoRequestDTO;
import br.edu.upe.sigep.dto.response.AgendamentoSessaoResponseDTO;
import br.edu.upe.sigep.enums.StatusPresenca;
import br.edu.upe.sigep.service.impl.AgendamentoSessaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agendamentos-sessoes")
@RequiredArgsConstructor
public class AgendamentoSessaoController {

    private final AgendamentoSessaoService agendamentoSessaoService;

    @GetMapping
    public ResponseEntity<List<AgendamentoSessaoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(agendamentoSessaoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoSessaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return agendamentoSessaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plano/{planoId}")
    public ResponseEntity<List<AgendamentoSessaoResponseDTO>> buscarPorPlano(@PathVariable Long planoId) {
        return ResponseEntity.ok(agendamentoSessaoService.buscarPorPlanoTratamentoOrdenado(planoId));
    }

    @GetMapping("/plano/{planoId}/presencas/{status}")
    public ResponseEntity<Long> contarPorPlanoEStatusPresenca(
            @PathVariable Long planoId,
            @PathVariable StatusPresenca status) {
        return ResponseEntity.ok(agendamentoSessaoService.contarPorPlanoEStatusPresenca(planoId, status));
    }

    @PostMapping
    public ResponseEntity<AgendamentoSessaoResponseDTO> salvar(
            @Valid @RequestBody AgendamentoSessaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agendamentoSessaoService.salvar(dto));
    }

    @PatchMapping("/{id}/presenca")
    public ResponseEntity<AgendamentoSessaoResponseDTO> registrarPresenca(
            @PathVariable Long id,
            @RequestBody RegistrarPresencaRequest request) {
        return ResponseEntity.ok(
                agendamentoSessaoService.registrarPresenca(id, request.status(), request.usuarioId()));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoSessaoResponseDTO> cancelarSessao(
            @PathVariable Long id,
            @RequestBody CancelarSessaoRequest request) {
        return ResponseEntity.ok(
                agendamentoSessaoService.cancelarSessao(id, request.motivo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long id) {
        agendamentoSessaoService.deletarPorId(id);
        return ResponseEntity.noContent().build();
    }

    public record RegistrarPresencaRequest(
            StatusPresenca status,
            Long usuarioId) {
    }

    public record CancelarSessaoRequest(String motivo) {
    }
}