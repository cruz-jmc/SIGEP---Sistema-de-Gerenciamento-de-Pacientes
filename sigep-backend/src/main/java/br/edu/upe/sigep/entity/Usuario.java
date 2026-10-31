package br.edu.upe.sigep.entity;

import br.edu.upe.sigep.enums.perfilUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;
    @Column(length = 150, nullable = false)
    private String nome;
    @Column(length = 150, nullable = false, unique = true)
    private String email;
    @Column(length = 250, nullable = false)
    private String senhaHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private perfilUsuario perfil;
    @Column(nullable = false)
    private boolean ativo;
    @Column(name= "primeiro_acesso", nullable = false)
    private boolean primeiroAcesso = true;
    @ManyToOne
    @JoinColumn(name = "criado_por_id")
    private Usuario criadorPor;
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
