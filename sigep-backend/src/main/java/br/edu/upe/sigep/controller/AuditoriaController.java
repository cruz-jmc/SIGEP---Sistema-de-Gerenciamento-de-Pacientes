package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.entity.Auditoria;
import br.edu.upe.sigep.service.impl.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @PostMapping
    public ResponseEntity<Auditoria> registrar(
            @RequestBody Auditoria auditoria) {

        return ResponseEntity.status(201)
                .body(auditoriaService.registrar(auditoria));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Auditoria>> listarPorUsuario(
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(
                auditoriaService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/tabela/{tabelaNome}")
    public ResponseEntity<List<Auditoria>> listarPorTabela(
            @PathVariable String tabelaNome) {

        return ResponseEntity.ok(
                auditoriaService.listarPorTabela(tabelaNome));
    }

    @GetMapping("/registro/{registroId}")
    public ResponseEntity<List<Auditoria>> listarPorRegistro(
            @PathVariable Long registroId) {

        return ResponseEntity.ok(
                auditoriaService.listarPorRegistro(registroId));
    }

    @GetMapping("/cronologica")
    public ResponseEntity<List<Auditoria>> listarCronologicamente() {

        return ResponseEntity.ok(
                auditoriaService.listarCronologicamente());
    }
}
