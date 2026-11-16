package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.request.SolicitacaoAgendamentoRequestDTO;
import br.edu.upe.sigep.dto.response.SolicitacaoAgendamentoResponseDTO;
import br.edu.upe.sigep.enums.StatusSolicitacao;
import br.edu.upe.sigep.service.impl.SolicitacaoAgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoAgendamentoController {

    private final SolicitacaoAgendamentoService solicitacaoService;

    @PostMapping
    public ResponseEntity<SolicitacaoAgendamentoResponseDTO> criar(
            @Valid @RequestBody SolicitacaoAgendamentoRequestDTO dto) {
        SolicitacaoAgendamentoResponseDTO criada = solicitacaoService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoAgendamentoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(solicitacaoService.listarTodas());
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<SolicitacaoAgendamentoResponseDTO>> listarPendentes() {
        return ResponseEntity.ok(solicitacaoService.listarPendentes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoAgendamentoResponseDTO> buscarPorId(
            @PathVariable Long id) {
        return solicitacaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<SolicitacaoAgendamentoResponseDTO>> buscarPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(
                solicitacaoService.buscarPorPaciente(pacienteId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SolicitacaoAgendamentoResponseDTO>> buscarPorStatus(
            @PathVariable StatusSolicitacao status) {
        return ResponseEntity.ok(
                solicitacaoService.buscarPorStatus(status));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<SolicitacaoAgendamentoResponseDTO> aprovar(
            @PathVariable Long id,
            @RequestParam Long revisorId) {
        return ResponseEntity.ok(
                solicitacaoService.aprovarSolicitacao(id, revisorId));
    }

    @PatchMapping("/{id}/rejeitar")
    public ResponseEntity<SolicitacaoAgendamentoResponseDTO> rejeitar(
            @PathVariable Long id,
            @RequestParam Long revisorId,
            @RequestBody String motivo) {
        return ResponseEntity.ok(
                solicitacaoService.rejeitarSolicitacao(id, revisorId, motivo));
    }
}