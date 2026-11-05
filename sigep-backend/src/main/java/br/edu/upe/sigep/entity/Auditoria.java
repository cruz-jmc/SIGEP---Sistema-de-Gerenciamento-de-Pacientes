package br.edu.upe.sigep.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "tabela_nome", length = 100, nullable = false)
    private String tabelaNome;

    @Column(name = "registro_id", nullable = false)
    private Long registroId;

    @Column(length = 50, nullable = false)
    private String acao;

    @Column(name = "dados_anteriores", columnDefinition = "jsonb")
    private String dadosAnteriores;

    @Column(name = "dados_novos", columnDefinition = "jsonb")
    private String dadosNovos;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
