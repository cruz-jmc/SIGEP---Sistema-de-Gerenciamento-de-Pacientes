package br.edu.upe.sigep.dto.request;

import br.edu.upe.sigep.enums.Grupos;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoAgendamentoRequestDTO {

    @NotNull(message = "O grupo é obrigatório")
    private Grupos grupo;

    @NotNull(message = "A data de atendimento inicial é obrigatória")
    private LocalDate dataAtendimentoInicial;

    @NotNull(message = "O ID do paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "O ID da especialidade é obrigatório")
    private Long especialidadeId;

    private Long profissionalId;

    @NotNull(message = "O ID do usuário que está cadastrando é obrigatório")
    private Long cadastradoPorId;
}