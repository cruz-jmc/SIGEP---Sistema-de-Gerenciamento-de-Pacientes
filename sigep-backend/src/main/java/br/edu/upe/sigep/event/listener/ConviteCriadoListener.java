package br.edu.upe.sigep.event.listener;

import br.edu.upe.sigep.event.ConviteCriadoEvent;
import br.edu.upe.sigep.service.NotificacaoConviteService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ConviteCriadoListener {

    private final NotificacaoConviteService notificacaoConviteService;

    public ConviteCriadoListener(
            NotificacaoConviteService notificacaoConviteService) {

        this.notificacaoConviteService = notificacaoConviteService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void processarConvite(ConviteCriadoEvent evento) {

        notificacaoConviteService.enviarConvite(
                evento.nome(),
                evento.email(),
                evento.token()
        );
    }
}