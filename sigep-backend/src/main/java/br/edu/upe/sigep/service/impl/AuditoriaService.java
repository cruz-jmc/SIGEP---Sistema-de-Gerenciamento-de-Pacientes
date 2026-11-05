package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.Auditoria;
import br.edu.upe.sigep.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;

    @Transactional
    public Auditoria registrar(Auditoria auditoria) {
        if (auditoria.getCriadoEm() == null) {
            auditoria.setCriadoEm(LocalDateTime.now());
        }
        return auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarPorUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarPorTabela(String tabelaNome) {
        return auditoriaRepository.findByTabelaNomeOrderByCriadoEmDesc(tabelaNome);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarPorRegistro(Long registroId) {
        return auditoriaRepository.findByRegistroIdOrderByCriadoEmDesc(registroId);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarCronologicamente() {
        return auditoriaRepository.findAllByOrderByCriadoEmAsc();
    }
}
