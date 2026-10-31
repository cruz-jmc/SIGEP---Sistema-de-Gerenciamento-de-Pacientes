package br.edu.upe.sigep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class ProfissionalEspecialidadeId implements Serializable {
    @Column(name = "profissional_id")
    private Long id;
    @Column(name = "especialidade_id")
    private Long id;





}
