package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.Especialidade;
import br.edu.upe.sigep.repository.EspecialidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EspecialidadeService {
    private final EspecialidadeRepository especialidadeRepository;

    @Transactional
    public Especialidade cadastrar(Especialidade especialidade) {
        validar(especialidade);
        if (especialidadeRepository.existsByNome(especialidade.getNome())) throw new IllegalArgumentException("Especialidade já cadastrada.");
        especialidade.setAtivo(true);
        return especialidadeRepository.save(especialidade);
    }
    @Transactional(readOnly = true) public List<Especialidade> listar() { return especialidadeRepository.findAll(); }
    @Transactional(readOnly = true) public List<Especialidade> listarAtivas() { return especialidadeRepository.findByAtivoTrue(); }
    @Transactional(readOnly = true) public Optional<Especialidade> buscarPorId(Long id) { return especialidadeRepository.findById(id); }
    @Transactional(readOnly = true) public Optional<Especialidade> buscarPorNome(String nome) { return especialidadeRepository.findByNome(nome); }
    @Transactional
    public Especialidade atualizar(Especialidade especialidade) {
        if (especialidade == null || especialidade.getId() == null || !especialidadeRepository.existsById(especialidade.getId())) throw new IllegalArgumentException("Especialidade não encontrada.");
        validar(especialidade);
        if (especialidadeRepository.existsByNomeAndIdNot(especialidade.getNome(), especialidade.getId())) throw new IllegalArgumentException("Especialidade já cadastrada.");
        return especialidadeRepository.save(especialidade);
    }
    @Transactional
    public Especialidade alterarAtivo(Long id, boolean ativo) {
        Especialidade especialidade = especialidadeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada."));
        especialidade.setAtivo(ativo);
        return especialidadeRepository.save(especialidade);
    }
    private void validar(Especialidade e) {
        if (e == null || vazio(e.getNome()) || vazio(e.getDescricao())) throw new IllegalArgumentException("Dados obrigatórios da especialidade não informados.");
    }
    private boolean vazio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
