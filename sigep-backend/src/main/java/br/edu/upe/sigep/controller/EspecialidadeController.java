package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.entity.Especialidade;
import br.edu.upe.sigep.dto.request.EspecialidadeRequestDTO;
import br.edu.upe.sigep.dto.response.EspecialidadeResponseDTO;
import br.edu.upe.sigep.mapper.EspecialidadeMapper;
import br.edu.upe.sigep.service.impl.EspecialidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadeController {

    private final EspecialidadeService service;

    @PostMapping
    public ResponseEntity<EspecialidadeResponseDTO> cadastrar(
            @Valid @RequestBody EspecialidadeRequestDTO entrada) {
        Especialidade cadastrado = service.cadastrar(EspecialidadeMapper.toEntity(entrada));
        return ResponseEntity
                .created(URI.create("/api/especialidades/" + cadastrado.getId()))
                .body(EspecialidadeMapper.toResponse(cadastrado));
    }

    @GetMapping
    public ResponseEntity<List<EspecialidadeResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream()
                .map(EspecialidadeMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadeResponseDTO> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(EspecialidadeMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<EspecialidadeResponseDTO>> listarAtivas() {
        return ResponseEntity.ok(service.listarAtivas().stream()
                .map(EspecialidadeMapper::toResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadeResponseDTO> atualizar(
            @PathVariable Long id, @Valid @RequestBody EspecialidadeRequestDTO entrada) {
        Especialidade entidade = EspecialidadeMapper.toEntity(entrada);
        entidade.setId(id);
        return ResponseEntity.ok(EspecialidadeMapper.toResponse(service.atualizar(entidade)));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<EspecialidadeResponseDTO> alterarAtivo(
            @PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(EspecialidadeMapper.toResponse(service.alterarAtivo(id, ativo)));
    }
}
