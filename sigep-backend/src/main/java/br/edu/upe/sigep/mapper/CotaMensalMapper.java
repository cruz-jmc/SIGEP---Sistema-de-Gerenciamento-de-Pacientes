package br.edu.upe.sigep.mapper;

import br.edu.upe.sigep.dto.request.CotaMensalRequestDTO;
import br.edu.upe.sigep.dto.response.CotaMensalResponseDTO;
import br.edu.upe.sigep.entity.CotaMensal;
import br.edu.upe.sigep.entity.Especialidade;
import br.edu.upe.sigep.entity.Profissional;
import br.edu.upe.sigep.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class CotaMensalMapper {

    public CotaMensal toEntity(CotaMensalRequestDTO dto, Profissional profissional, Especialidade especialidade, Usuario usuario) {
        CotaMensal cota = new CotaMensal();
        cota.setProfissional(profissional);
        cota.setEspecialidade(especialidade);
        cota.setMesReferencia(dto.getMesReferencia());
        cota.setAnoReferencia(dto.getAnoReferencia());
        cota.setQtdCotaLimite(dto.getQtdCotaLimite());
        cota.setUsuario(usuario);
        return cota;
    }

    public CotaMensalResponseDTO toResponseDTO(CotaMensal entity) {
        return new CotaMensalResponseDTO(
                entity.getId(),
                entity.getProfissional().getId(),
                entity.getProfissional().getNome(),
                entity.getEspecialidade().getId(),
                entity.getEspecialidade().getNome(),
                entity.getMesReferencia(),
                entity.getAnoReferencia(),
                entity.getQtdCotaLimite()
        );
    }
}