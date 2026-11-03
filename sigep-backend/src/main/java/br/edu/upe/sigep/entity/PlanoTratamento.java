package br.edu.upe.sigep.entity;

import br.edu.upe.sigep.enums.statusTratamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plano_tratamento")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlanoTratamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private statusTratamento statusPlano;
    @Column(name = "aprovado_em")
    private LocalDateTime aprovadoEm;
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToOne
    @JoinColumn(name = "solicitacao_id",nullable = false, unique = true)
    private SolicitacaoAgendamento solicitaçao;

    @ManyToOne
    @JoinColumn(name = "aprovado_por_id",nullable = false)
    private Usuario aprovadoPor;
}
