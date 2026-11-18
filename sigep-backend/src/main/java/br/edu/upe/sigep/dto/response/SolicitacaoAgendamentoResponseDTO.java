package br.edu.upe.sigep.dto.response;

import br.edu.upe.sigep.enums.Grupos;
import br.edu.upe.sigep.enums.StatusSolicitacao;
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
public class SolicitacaoAgendamentoResponseDTO {

    private Long id;
    private Grupos grupo;
    private LocalDate dataAtendimentoInicial;
    private StatusSolicitacao statusSolicitacao;
    private String motivoRejeicao;
    private LocalDateTime criadoEm;
    private LocalDateTime revisadoEm;

    private Long pacienteId;
    private String pacienteNome;

    private Long especialidadeId;
    private String especialidadeNome;

    private Long profissionalId;
    private String profissionalNome;

    private Long cadastradoPorId;
    private Long analisadoPorId;
}