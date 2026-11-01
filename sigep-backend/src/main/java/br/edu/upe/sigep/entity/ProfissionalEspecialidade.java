package br.edu.upe.sigep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profissional_especialidade")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfissionalEspecialidade {
    @EmbeddedId
    private ProfissionalEspecialidadeId id=  new ProfissionalEspecialidadeId();

    @ManyToOne
    @MapsId("profissionalId")
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @MapsId("especialidadeId")
    @JoinColumn(name = "especialidade_id", nullable = false)
    private Especialidade especialidade;



}
