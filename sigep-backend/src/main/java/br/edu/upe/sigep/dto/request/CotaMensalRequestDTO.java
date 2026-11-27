package br.edu.upe.sigep.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CotaMensalRequestDTO {

    @NotNull(message = "O ID do profissional é obrigatório.")
    private Long profissionalId;

    @NotNull(message = "O ID da especialidade é obrigatório.")
    private Long especialidadeId;

    @NotNull(message = "O mês de referência é obrigatório.")
    @Min(value = 1, message = "O mês de referência deve estar entre 1 e 12.")
    @Max(value = 12, message = "O mês de referência deve estar entre 1 e 12.")
    private Short mesReferencia;

    @NotNull(message = "O ano de referência é obrigatório.")
    @Min(value = 2024, message = "Ano de referência inválido.")
    private Short anoReferencia;

    @NotNull(message = "A quantidade limite da cota é obrigatória.")
    @PositiveOrZero(message = "A quantidade limite da cota não pode ser negativa.")
    private Integer qtdCotaLimite;
}