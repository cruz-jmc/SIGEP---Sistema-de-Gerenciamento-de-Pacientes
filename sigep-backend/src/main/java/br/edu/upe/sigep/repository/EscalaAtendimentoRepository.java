package br.edu.upe.sigep.repository;

import br.edu.upe.sigep.entity.EscalaAtendimento;
import br.edu.upe.sigep.enums.DiaSemana;
import br.edu.upe.sigep.enums.Grupos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EscalaAtendimentoRepository extends JpaRepository<EscalaAtendimento, Long> {

    List<EscalaAtendimento> findByAtivoTrue();

    List<EscalaAtendimento> findByProfissionalIdAndAtivoTrue(Long profissionalId);

    @Query("""
           SELECT e FROM EscalaAtendimento e
           WHERE e.profissional.id = :profissionalId
             AND e.especialidade.id = :especialidadeId
             AND e.diaSemana = :diaSemana
             AND e.ativo = true
             AND :data BETWEEN e.vigenciaInicio AND e.vigenciaFim
           """)
    Optional<EscalaAtendimento> buscarEscalaValida(
            @Param("profissionalId") Long profissionalId,
            @Param("especialidadeId") Long especialidadeId,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("data") LocalDate data);

    @Query("""
           SELECT e FROM EscalaAtendimento e
           WHERE e.profissional.id = :profissionalId
             AND e.especialidade.id = :especialidadeId
             AND e.grupo = :grupo
             AND e.diaSemana = :diaSemana
             AND e.ativo = true
             AND :data BETWEEN e.vigenciaInicio AND e.vigenciaFim
           """)
    Optional<EscalaAtendimento> buscarEscalaValidaPorGrupo(
            @Param("profissionalId") Long profissionalId,
            @Param("especialidadeId") Long especialidadeId,
            @Param("grupo") Grupos grupo,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("data") LocalDate data);
}