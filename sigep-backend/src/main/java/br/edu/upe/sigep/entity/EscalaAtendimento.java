package br.edu.upe.sigep.entity;

import br.edu.upe.sigep.enums.Grupos;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "escala_atendimento")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EscalaAtendimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grupos grupo;
    @Column(name = "dia_semana", nullable = false)
    private Short diaSemana;
    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;
    @Column(name = "vigencia_fim", nullable = false)
    private LocalDate vigenciaFim;
    @Column(name = "pacientes_por_turno", nullable = false)
    private Integer pacientesPorTurno;
    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne
    @JoinColumn(name ="profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name ="especialidade_id", nullable = false)
    private Especialidade especialidade;





}
