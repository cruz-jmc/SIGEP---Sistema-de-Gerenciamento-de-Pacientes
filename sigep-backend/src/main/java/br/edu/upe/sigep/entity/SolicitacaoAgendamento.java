package br.edu.upe.sigep.entity;

import br.edu.upe.sigep.enums.Grupos;
import br.edu.upe.sigep.enums.StatusSolicitacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_agendamento")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SolicitacaoAgendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grupos grupo;
    @Column(name = "data_agendamento_inicial", nullable = false)
    private LocalDate dataAtendimentoInicial;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao statusSolicitacao;
    @Column(name = "motivo_rejeicao", columnDefinition = "text")
    private String motivoRejeicao;
    @Column(name =  "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    @Column(name =  "revisado_em")
    private LocalDateTime revisadoEm;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "especialidade_id", nullable = false)
    private Especialidade especialidade;

    @ManyToOne
    @JoinColumn(name = "profissional_solicitado_id")
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "cadastrado_por", nullable = false)
    private Usuario cadastradoPor;

    @ManyToOne
    @JoinColumn(name = "revisado_por")
    private Usuario analisadoPor;



}
