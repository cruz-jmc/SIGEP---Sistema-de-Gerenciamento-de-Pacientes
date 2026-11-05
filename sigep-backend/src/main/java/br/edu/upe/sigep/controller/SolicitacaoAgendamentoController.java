
package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.entity.SolicitacaoAgendamento;
import br.edu.upe.sigep.enums.statusSolicitacao;
import br.edu.upe.sigep.service.impl.SolicitacaoAgendamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoAgendamentoController {

    private final SolicitacaoAgendamentoService solicitacaoService;

    @PostMapping
    public ResponseEntity<SolicitacaoAgendamento> criar(
            @RequestBody SolicitacaoAgendamento solicitacao) {
        return ResponseEntity.ok(solicitacaoService.salvar(solicitacao));
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoAgendamento>> listarTodas() {
        return ResponseEntity.ok(solicitacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoAgendamento> buscarPorId(
            @PathVariable Long id) {
        return solicitacaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<SolicitacaoAgendamento>> buscarPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(
                solicitacaoService.buscarPorPaciente(pacienteId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SolicitacaoAgendamento>> buscarPorStatus(
            @PathVariable statusSolicitacao status) {
        return ResponseEntity.ok(
                solicitacaoService.buscarPorStatus(status));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<SolicitacaoAgendamento> aprovar(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                solicitacaoService.aprovarSolicitacao(id));
    }

    @PatchMapping("/{id}/rejeitar")
    public ResponseEntity<SolicitacaoAgendamento> rejeitar(
            @PathVariable Long id,
            @RequestBody String motivo) {
        return ResponseEntity.ok(
                solicitacaoService.rejeitarSolicitacao(id, motivo));
    }
}
