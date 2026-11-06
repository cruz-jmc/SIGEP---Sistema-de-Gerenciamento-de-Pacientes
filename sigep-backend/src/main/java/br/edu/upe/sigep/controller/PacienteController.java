package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.entity.Paciente;
import br.edu.upe.sigep.dto.request.PacienteRequestDTO;
import br.edu.upe.sigep.dto.response.PacienteResponseDTO;
import br.edu.upe.sigep.mapper.PacienteMapper;
import br.edu.upe.sigep.service.impl.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService service;

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> cadastrar(
            @Valid @RequestBody PacienteRequestDTO entrada) {
        Paciente cadastrado = service.cadastrar(PacienteMapper.toEntity(entrada));
        return ResponseEntity
                .created(URI.create("/api/pacientes/" + cadastrado.getId()))
                .body(PacienteMapper.toResponse(cadastrado));
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream()
                .map(PacienteMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(PacienteMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<PacienteResponseDTO>> listarAtivos() {
        return ResponseEntity.ok(service.listarAtivos().stream()
                .map(PacienteMapper::toResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> atualizar(
            @PathVariable Long id, @Valid @RequestBody PacienteRequestDTO entrada) {
        Paciente entidade = PacienteMapper.toEntity(entrada);
        entidade.setId(id);
        return ResponseEntity.ok(PacienteMapper.toResponse(service.atualizar(entidade)));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<PacienteResponseDTO> alterarAtivo(
            @PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(PacienteMapper.toResponse(service.alterarAtivo(id, ativo)));
    }
}
