package br.edu.upe.sigep.entity;


import br.edu.upe.sigep.enums.statusDataDisponivel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_disponivel_realocacao")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DataDisponivelRealocacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "data_disponivel")
    private LocalDate dataDisponivel;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private statusDataDisponivel status;
    @Column(name =  "disponibilizada_em", nullable = false, updatable = false)
    private LocalDateTime disponibilizadaEm;
    @Column(name = "realocada_em")
    private LocalDateTime realocadaEm;

    @ManyToOne
    @JoinColumn(name = "escala_atendimento_id", nullable = false)
    private EscalaAtendimento escalaAtendimento;

    @ManyToOne
    @JoinColumn(name = "plano_origem_id")
    private PlanoTratamento planoOrigem;

    @ManyToOne
    @JoinColumn(name = "agendamento_origem_id")
    private AgendamentoSessao agendamentoOrigem;

    @ManyToOne
    @JoinColumn(name = "agendamento_destino_id")
    private AgendamentoSessao agendamentoDestino;

    @ManyToOne
    @JoinColumn(name = "cadastrado_por_id", nullable = false)
    private Usuario cadastradoPor;



}
