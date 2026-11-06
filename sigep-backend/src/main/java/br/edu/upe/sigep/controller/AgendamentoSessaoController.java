
package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.entity.AgendamentoSessao;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.StatusPresenca;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.impl.AgendamentoSessaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agendamentos-sessoes")
@RequiredArgsConstructor
public class AgendamentoSessaoController {

    private final AgendamentoSessaoService agendamentoSessaoService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<AgendamentoSessao>> listarTodos() {
        return ResponseEntity.ok(
                agendamentoSessaoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoSessao> buscarPorId(
            @PathVariable Long id) {
        return agendamentoSessaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plano/{planoId}")
    public ResponseEntity<List<AgendamentoSessao>> buscarPorPlano(
            @PathVariable Long planoId) {
        return ResponseEntity.ok(
                agendamentoSessaoService.buscarPorPlanoTratamentoOrdenado(planoId));
    }

    @GetMapping("/plano/{planoId}/presencas/{status}")
    public ResponseEntity<Long> contarPorPlanoEStatusPresenca(
            @PathVariable Long planoId,
            @PathVariable StatusPresenca status) {
        return ResponseEntity.ok(
                agendamentoSessaoService.contarPorPlanoEStatusPresenca(planoId, status));
    }

    @PostMapping
    public ResponseEntity<AgendamentoSessao> salvar(
            @RequestBody AgendamentoSessao agendamentoSessao) {
        return ResponseEntity.ok(
                agendamentoSessaoService.salvar(agendamentoSessao));
    }

    @PatchMapping("/{id}/presenca")
    public ResponseEntity<AgendamentoSessao> registrarPresenca(
            @PathVariable Long id,
            @RequestBody RegistrarPresencaRequest request) {

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                agendamentoSessaoService.registrarPresenca(
                        id, request.status(), usuario));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoSessao> cancelarSessao(
            @PathVariable Long id,
            @RequestBody CancelarSessaoRequest request) {
        return ResponseEntity.ok(
                agendamentoSessaoService.cancelarSessao(
                        id, request.motivo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorId(
            @PathVariable Long id) {
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
