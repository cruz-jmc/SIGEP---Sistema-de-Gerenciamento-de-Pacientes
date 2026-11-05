package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.service.impl.CotaMensalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cotas-mensais")
@RequiredArgsConstructor
public class CotaMensalController {

    private final CotaMensalService cotaMensalService;

    @GetMapping("/disponibilidade")
    public ResponseEntity<Boolean> temCotaDisponivel(
            @RequestParam Long especialidadeId,
            @RequestParam LocalDate data) {

        return ResponseEntity.ok(
                cotaMensalService.temCotaDisponivel(especialidadeId, data));
    }

    @PatchMapping("/decrementar")
    public ResponseEntity<Void> decrementarCota(
            @RequestParam Long especialidadeId,
            @RequestParam LocalDate data) {

        cotaMensalService.decrementarCota(especialidadeId, data);

        return ResponseEntity.noContent().build();
    }
}
