package br.edu.upe.sigep.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealocacaoRequestDTO {

    @NotNull(message = "O ID do agendamento de destino é obrigatório.")
    private Long agendamentoDestinoId;
}