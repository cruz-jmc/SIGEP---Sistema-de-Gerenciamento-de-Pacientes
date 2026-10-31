package br.edu.upe.sigep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profissional")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Profissional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 200, nullable = false)
    private String nome;
    @Column(length = 200, nullable = false)
    private String conselho;
    @Column(name ="registro_conselho", length = 50, nullable = false, unique = true)
    private String registroConselho;
    @Column(nullable = false)
    private boolean ativo = true;

}
