package br.edu.upe.sigep.service;

public interface NotificacaoConviteService {

    void enviarConvite(
            String nome,
            String email,
            String token
    );
}