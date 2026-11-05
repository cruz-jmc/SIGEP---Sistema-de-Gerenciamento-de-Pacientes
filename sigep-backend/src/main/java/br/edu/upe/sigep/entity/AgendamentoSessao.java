package br.edu.upe.sigep.entity;

import br.edu.upe.sigep.enums.StatusPresenca;
import br.edu.upe.sigep.enums.StatusSessao;
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
    private Short numeroSessao;

    @Column(nullable = false)
    private LocalDate dataAgendada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSessao status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPresenca statusPresenca;

    @Column(name = "presenca_registrada_em")
    private LocalDateTime presencaRegistradaEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @Column(name = "motivo_cancelamento", columnDefinition = "text")
    private String motivoCancelamento;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "presenca_registrada_por")
    private Usuario presencaRegistradaPor;
}
