package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.Profissional;
import br.edu.upe.sigep.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfissionalService {
    private final ProfissionalRepository profissionalRepository;

    @Transactional
    public Profissional cadastrar(Profissional profissional) {
        validar(profissional);
        if (profissionalRepository.existsByRegistroConselho(profissional.getRegistroConselho())) throw new IllegalArgumentException("Registro do conselho já cadastrado.");
        profissional.setAtivo(true);
        profissional.setCriadoEm(LocalDateTime.now());
        return profissionalRepository.save(profissional);
    }
    @Transactional(readOnly = true) public List<Profissional> listar() { return profissionalRepository.findAll(); }
    @Transactional(readOnly = true) public List<Profissional> listarAtivos() { return profissionalRepository.findByAtivoTrue(); }
    @Transactional(readOnly = true) public Optional<Profissional> buscarPorId(Long id) { return profissionalRepository.findById(id); }
    @Transactional(readOnly = true) public Optional<Profissional> buscarPorRegistroConselho(String registro) { return profissionalRepository.findByRegistroConselho(registro); }
    @Transactional
    public Profissional atualizar(Profissional profissional) {
        if (profissional == null || profissional.getId() == null || !profissionalRepository.existsById(profissional.getId())) throw new IllegalArgumentException("Profissional não encontrado.");
        validar(profissional);
        if (profissionalRepository.existsByRegistroConselhoAndIdNot(profissional.getRegistroConselho(), profissional.getId())) throw new IllegalArgumentException("Registro do conselho já cadastrado.");
        return profissionalRepository.save(profissional);
    }
    @Transactional
    public Profissional alterarAtivo(Long id, boolean ativo) {
        Profissional profissional = profissionalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));
        profissional.setAtivo(ativo);
        return profissionalRepository.save(profissional);
    }
    private void validar(Profissional p) {
        if (p == null || vazio(p.getNome()) || vazio(p.getConselho()) || vazio(p.getRegistroConselho())) throw new IllegalArgumentException("Dados obrigatórios do profissional não informados.");
    }
    private boolean vazio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
