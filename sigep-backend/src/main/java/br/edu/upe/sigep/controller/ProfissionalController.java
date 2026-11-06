package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.entity.Profissional;
import br.edu.upe.sigep.dto.request.ProfissionalRequestDTO;
import br.edu.upe.sigep.dto.response.ProfissionalResponseDTO;
import br.edu.upe.sigep.mapper.ProfissionalMapper;
import br.edu.upe.sigep.service.impl.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService service;

    @PostMapping
    public ResponseEntity<ProfissionalResponseDTO> cadastrar(
            @Valid @RequestBody ProfissionalRequestDTO entrada) {
        Profissional cadastrado = service.cadastrar(ProfissionalMapper.toEntity(entrada));
        return ResponseEntity
                .created(URI.create("/api/profissionais/" + cadastrado.getId()))
                .body(ProfissionalMapper.toResponse(cadastrado));
    }

    @GetMapping
    public ResponseEntity<List<ProfissionalResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream()
                .map(ProfissionalMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ProfissionalMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<ProfissionalResponseDTO>> listarAtivos() {
        return ResponseEntity.ok(service.listarAtivos().stream()
                .map(ProfissionalMapper::toResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> atualizar(
            @PathVariable Long id, @Valid @RequestBody ProfissionalRequestDTO entrada) {
        Profissional entidade = ProfissionalMapper.toEntity(entrada);
        entidade.setId(id);
        return ResponseEntity.ok(ProfissionalMapper.toResponse(service.atualizar(entidade)));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<ProfissionalResponseDTO> alterarAtivo(
            @PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(ProfissionalMapper.toResponse(service.alterarAtivo(id, ativo)));
    }
}
