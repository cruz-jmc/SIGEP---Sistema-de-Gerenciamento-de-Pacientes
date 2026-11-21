package br.edu.upe.sigep.event;

public record ConviteCriadoEvent(
        String nome,
        String email,
        String token
) {
}