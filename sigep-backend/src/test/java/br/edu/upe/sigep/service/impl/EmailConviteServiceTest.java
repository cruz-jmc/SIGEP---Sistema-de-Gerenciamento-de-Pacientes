package br.edu.upe.sigep.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailConviteServiceTest {

    private static final String REMETENTE = "convites@sigep.example";
    private static final String DESTINATARIO = "pessoa@sigep.example";
    private static final String NOME = "Pessoa Teste";
    private static final String TOKEN = "token-ficticio";

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final EmailConviteService service = new EmailConviteService(mailSender, REMETENTE);

    @Test
    void deveEnviarMensagemDeAtivacaoComTokenEPrazo() {
        service.enviarConvite(NOME, DESTINATARIO, TOKEN);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage mensagem = captor.getValue();

        assertEquals(REMETENTE, mensagem.getFrom());
        assertArrayEquals(new String[]{DESTINATARIO}, mensagem.getTo());
        assertEquals("SIGEP - Ativação de conta", mensagem.getSubject());
        String corpo = mensagem.getText();
        assertNotNull(corpo);
        assertTrue(corpo.contains("Olá, " + NOME + "!"));
        assertTrue(corpo.contains("Sua conta no SIGEP foi cadastrada."));
        assertTrue(corpo.lines().anyMatch(TOKEN::equals));
        assertTrue(corpo.contains("O convite é válido por 24 horas."));
    }

    @Test
    void devePropagarFalhaDoEnvio() {
        MailSendException falha = new MailSendException("SMTP indisponível");
        doThrow(falha).when(mailSender).send(any(SimpleMailMessage.class));

        assertSame(falha, assertThrows(MailSendException.class,
                () -> service.enviarConvite(NOME, DESTINATARIO, TOKEN)));
    }
}
