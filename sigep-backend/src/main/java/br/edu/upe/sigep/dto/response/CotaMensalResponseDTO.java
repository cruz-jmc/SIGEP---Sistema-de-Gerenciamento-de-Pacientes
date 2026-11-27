package br.edu.upe.sigep.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CotaMensalResponseDTO {
    private Long id;
    private Long profissionalId;
    private String profissionalNome;
    private Long especialidadeId;
    private String especialidadeNome;
    private Short mesReferencia;
    private Short anoReferencia;
    private Integer qtdCotaLimite;
}