package br.edu.upe.sigep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "Paciente")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nome_completo", length = 200, nullable = false)
    private String nomeCompleto;
    @Column(length = 11, nullable = false, unique = true)
    private String cpf;
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;
    @Column(name = "cartao_sus", length = 15, nullable = false, unique = true)
    private String cartaoSus;
    @Column(length = 20, nullable = false)
    private String telefone;
    @Column(name = "ubs_origem",length = 200, nullable = false)
    private String ubsOrigem;
    @Column(nullable = false)
    private boolean ativo = true;
    @ManyToOne
    @JoinColumn(name = "cadastrado_por_id")
    private Usuario cadastradoPor;
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
