package br.edu.upe.sigep.dto.response;

import br.edu.upe.sigep.enums.DiaSemana;
import br.edu.upe.sigep.enums.Grupos;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EscalaAtendimentoResponseDTO {

    private Long id;
    private Grupos grupo;
    private DiaSemana diaSemana;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private Integer pacientesPorTurno;
    private boolean ativo;
    private Long profissionalId;
    private String profissionalNome;
    private Long especialidadeId;
    private String especialidadeNome;
}