package br.edu.upe.sigep.entity;

import br.edu.upe.sigep.enums.TipoNotificacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NotificacaoUsuario {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   @Enumerated(EnumType.STRING)
   @Column(name = "tipo_notificacao", nullable = false)
   private TipoNotificacao tipo;
   @Column(columnDefinition = "text", nullable = false)
   private String mensagem;
   @Column(nullable = false)
   private boolean lida = false;
   @Column(name="criado_em", nullable = false, updatable = false)
   private LocalDateTime criadaEm;
   @Column(name = "lido_em")
   private LocalDateTime lidoEm;

   @ManyToOne
   @JoinColumn(name =  "destinatario_id", nullable = false)
   private Usuario destinatario;

   @ManyToOne
   @JoinColumn(name = "plano_id")
   private PlanoTratamento plano;
}
