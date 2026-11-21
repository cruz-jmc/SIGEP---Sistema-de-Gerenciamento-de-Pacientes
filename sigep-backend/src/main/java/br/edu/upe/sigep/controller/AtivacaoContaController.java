package br.edu.upe.sigep.controller;

import br.edu.upe.sigep.dto.request.AtivacaoContaRequestDTO;
import br.edu.upe.sigep.service.ConviteAtivacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ativacao")
public class AtivacaoContaController {

    private final ConviteAtivacaoService conviteAtivacaoService;

    public AtivacaoContaController(
            ConviteAtivacaoService conviteAtivacaoService) {
        this.conviteAtivacaoService = conviteAtivacaoService;
    }

    @PostMapping
    public ResponseEntity<Void> ativar(
            @Valid @RequestBody AtivacaoContaRequestDTO dados) {

        conviteAtivacaoService.ativarConta(
                dados.token(),
                dados.senha()
        );

        return ResponseEntity.noContent().build();
    }
}