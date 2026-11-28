package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.request.RealocacaoRequestDTO;
import br.edu.upe.sigep.dto.response.DataDisponivelRealocacaoResponseDTO;
import br.edu.upe.sigep.enums.CriterioPriorizacaoRealocacao;
import br.edu.upe.sigep.service.impl.DataDisponivelRealocacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/realocacoes")
@RequiredArgsConstructor
public class DataDisponivelRealocacaoController {

    private final DataDisponivelRealocacaoService realocacaoService;

    @GetMapping("/disponiveis")
    public ResponseEntity<List<DataDisponivelRealocacaoResponseDTO>> listarDisponiveis(
            @RequestParam(required = false, defaultValue = "DATA_MAIS_PROXIMA") CriterioPriorizacaoRealocacao criterio) {
        return ResponseEntity.ok(realocacaoService.listarDisponiveis(criterio));
    }

    @GetMapping("/sugerir")
    public ResponseEntity<DataDisponivelRealocacaoResponseDTO> sugerirMelhorVaga(
            @RequestParam(required = false, defaultValue = "DATA_MAIS_PROXIMA") CriterioPriorizacaoRealocacao criterio) {
        return realocacaoService.sugerirMelhorVaga(criterio)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PatchMapping("/{id}/realocar")
    public ResponseEntity<DataDisponivelRealocacaoResponseDTO> realocar(
            @PathVariable Long id,
            @Valid @RequestBody RealocacaoRequestDTO dto) {

        return ResponseEntity.ok(realocacaoService.realocar(id, dto));
    }
}