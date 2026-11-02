package br.edu.upe.sigep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cota_mensal")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CotaMensal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "mes_referencia", nullable = false)
    private Integer mesReferencia;
    @Column(name = "ano_referencia", nullable = false)
    private Integer anoReferencia;
    @Column(name = "quantidade_cota_limite", nullable = false)
    private Integer qtdCotaLimite;

    @ManyToOne
    @JoinColumn(name = "especialidade_id", nullable = false)
    private Especialidade especialidade;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "cadastrado_por_id", nullable = false)
    private Usuario usuario;

}
