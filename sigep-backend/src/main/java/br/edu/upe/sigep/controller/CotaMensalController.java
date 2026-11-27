package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.request.CotaMensalRequestDTO;
import br.edu.upe.sigep.dto.response.CotaMensalResponseDTO;
import br.edu.upe.sigep.service.impl.CotaMensalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/cotas-mensais", "/api/cotas"})
@RequiredArgsConstructor
public class CotaMensalController {

    private final CotaMensalService cotaMensalService;

    @PostMapping
    public ResponseEntity<CotaMensalResponseDTO> cadastrar(@Valid @RequestBody CotaMensalRequestDTO dto) {
        CotaMensalResponseDTO criada = cotaMensalService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<CotaMensalResponseDTO>> consultar(
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Long especialidadeId,
            @RequestParam(required = false) Short mesReferencia,
            @RequestParam(required = false) Short anoReferencia) {

        return ResponseEntity.ok(
                cotaMensalService.consultar(profissionalId, especialidadeId, mesReferencia, anoReferencia));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CotaMensalResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cotaMensalService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CotaMensalResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CotaMensalRequestDTO dto) {

        return ResponseEntity.ok(cotaMensalService.atualizar(id, dto));
    }

    @GetMapping("/disponibilidade")
    public ResponseEntity<Boolean> temCotaDisponivel(
            @RequestParam Long profissionalId,
            @RequestParam Long especialidadeId,
            @RequestParam LocalDate data) {

        return ResponseEntity.ok(
                cotaMensalService.temCotaDisponivel(profissionalId, especialidadeId, data));
    }

    @PatchMapping("/decrementar")
    public ResponseEntity<Void> decrementarCota(
            @RequestParam Long profissionalId,
            @RequestParam Long especialidadeId,
            @RequestParam LocalDate data) {

        cotaMensalService.decrementarCota(profissionalId, especialidadeId, data);
        return ResponseEntity.noContent().build();
    }
}