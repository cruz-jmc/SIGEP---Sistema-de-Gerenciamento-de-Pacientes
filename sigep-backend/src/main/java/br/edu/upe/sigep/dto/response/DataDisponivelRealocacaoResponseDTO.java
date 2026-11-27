package br.edu.upe.sigep.dto.response;

import br.edu.upe.sigep.enums.StatusDataDisponivel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataDisponivelRealocacaoResponseDTO {
    private Long id;
    private LocalDate dataDisponivel;
    private StatusDataDisponivel status;
    private LocalDateTime disponibilizadaEm;
    private LocalDateTime realocadaEm;
    private Long escalaAtendimentoId;
    private Long profissionalId;
    private String profissionalNome;
    private Long especialidadeId;
    private String especialidadeNome;
    private Long planoOrigemId;
    private Long agendamentoOrigemId;
    private Long agendamentoDestinoId;
    private Long cadastradoPorId;
}