package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.service.NotificacaoConviteService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailConviteService implements NotificacaoConviteService {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailConviteService(
            JavaMailSender mailSender,
            @Value("${sigep.mail.from}") String remetente) {

        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @Override
    public void enviarConvite(
            String nome,
            String email,
            String token) {

        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(email);
        mensagem.setSubject("SIGEP - Ativação de conta");

        mensagem.setText(
                "Olá, " + nome + "!\n\n" +
                        "Sua conta no SIGEP foi cadastrada.\n\n" +
                        "Utilize o token abaixo para ativar sua conta:\n\n" +
                        token + "\n\n" +
                        "O convite é válido por 24 horas.\n\n" +
                        "Se você não esperava este convite, ignore esta mensagem.\n\n" +
                        "Equipe SIGEP"
        );

        mailSender.send(mensagem);
    }
}