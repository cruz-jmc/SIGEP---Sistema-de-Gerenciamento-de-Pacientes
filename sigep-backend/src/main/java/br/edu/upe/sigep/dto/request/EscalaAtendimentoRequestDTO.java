package br.edu.upe.sigep.dto.request;

import br.edu.upe.sigep.enums.DiaSemana;
import br.edu.upe.sigep.enums.Grupos;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EscalaAtendimentoRequestDTO {

    @NotNull(message = "O grupo é obrigatório.")
    private Grupos grupo;

    @NotNull(message = "O dia da semana é obrigatório.")
    private DiaSemana diaSemana;

    @NotNull(message = "A data de início da vigência é obrigatória.")
    private LocalDate vigenciaInicio;

    @NotNull(message = "A data de fim da vigência é obrigatória.")
    private LocalDate vigenciaFim;

    @NotNull(message = "A quantidade de pacientes por turno é obrigatória.")
    @Min(value = 1, message = "A quantidade de pacientes por turno deve ser maior que zero.")
    private Integer pacientesPorTurno;

    private Boolean ativo = true;

    @NotNull(message = "O ID do profissional é obrigatório.")
    private Long profissionalId;

    @NotNull(message = "O ID da especialidade é obrigatório.")
    private Long especialidadeId;
}