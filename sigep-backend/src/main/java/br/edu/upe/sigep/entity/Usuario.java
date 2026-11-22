package br.edu.upe.sigep.entity;

import br.edu.upe.sigep.enums.PerfilUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Long id;
    @Column(length = 150, nullable = false)
    private String nome;
    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "senha_hash", length = 255, nullable = false)
    private String senhaHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilUsuario perfil;
    @Column(nullable = false)
    private boolean ativo;

    // Preserva a leitura do campo legado em módulos que serializam associações com Usuario.
    @Column(name = "primeiro_acesso", nullable = false, insertable = false, updatable = false)
    private boolean primeiroAcesso;
    @JsonIgnore
    @Column(name = "revisao_credenciais", nullable = false)
    private long revisaoCredenciais;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por")
    private Usuario criadoPor;
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}
