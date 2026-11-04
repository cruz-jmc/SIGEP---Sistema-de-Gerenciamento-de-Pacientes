package br.edu.upe.sigep.entity;

import br.edu.upe.sigep.enums.statusPresenca;
import br.edu.upe.sigep.enums.statusSessao;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento_sessao")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AgendamentoSessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoTratamento planoTratamento;

    @ManyToOne
    @JoinColumn(name = "escala_atendimento_id", nullable = false)
    private EscalaAtendimento escalaAtendimento;

    @Column(nullable = false)
    private Integer numeroSessao;

    @Column(nullable = false)
    private LocalDate dataAgendada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private statusSessao status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private statusPresenca statusPresenca;

    private LocalDateTime presencaRegistradaEm;

    private LocalDateTime canceladoEm;

    @Column(name = "motivo_cancelamento", length = 250)
    private String motivoCancelamento;

    @Column( nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "presenca_registrada_por_id")
    private Usuario presencaRegistradaPor;
}