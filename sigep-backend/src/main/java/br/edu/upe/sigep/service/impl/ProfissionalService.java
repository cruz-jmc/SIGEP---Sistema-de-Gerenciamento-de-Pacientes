package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.Profissional;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.repository.ProfissionalRepository;
import br.edu.upe.sigep.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Profissional cadastrar(Profissional profissional) {
        validar(profissional);
        if (profissional.getId() != null) throw new IllegalArgumentException("O ID não deve ser informado no cadastro.");
        if (profissionalRepository.existsByRegistroConselho(profissional.getRegistroConselho())) throw new IllegalArgumentException("Registro do conselho já cadastrado.");
        profissional.setAtivo(true);
        profissional.setCriadoEm(LocalDateTime.now());
        profissional.setCadastradoPor(resolverCadastrador(profissional.getCadastradoPor()));
        return profissionalRepository.save(profissional);
    }
    @Transactional(readOnly = true) public List<Profissional> listar() { return profissionalRepository.findAll(); }
    @Transactional(readOnly = true) public List<Profissional> listarAtivos() { return profissionalRepository.findByAtivoTrue(); }
    @Transactional(readOnly = true) public Optional<Profissional> buscarPorId(Long id) { return profissionalRepository.findById(id); }
    @Transactional(readOnly = true) public Optional<Profissional> buscarPorRegistroConselho(String registro) { return profissionalRepository.findByRegistroConselho(registro); }
    @Transactional
    public Profissional atualizar(Profissional profissional) {
        if (profissional == null || profissional.getId() == null) throw new IllegalArgumentException("Profissional não encontrado.");
        Profissional existente = profissionalRepository.findById(profissional.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));
        validar(profissional);
        if (profissionalRepository.existsByRegistroConselhoAndIdNot(profissional.getRegistroConselho(), profissional.getId())) throw new IllegalArgumentException("Registro do conselho já cadastrado.");
        existente.setNome(profissional.getNome());
        existente.setConselho(profissional.getConselho());
        existente.setRegistroConselho(profissional.getRegistroConselho());
        return profissionalRepository.save(existente);
    }
    @Transactional
    public Profissional alterarAtivo(Long id, boolean ativo) {
        Profissional profissional = profissionalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));
        profissional.setAtivo(ativo);
        return profissionalRepository.save(profissional);
    }
    private void validar(Profissional p) {
        if (p == null || vazio(p.getNome()) || vazio(p.getConselho()) || vazio(p.getRegistroConselho())) throw new IllegalArgumentException("Dados obrigatórios do profissional não informados.");
        validarTamanho(p.getNome(), 200, "Nome");
        validarTamanho(p.getConselho(), 200, "Conselho");
        validarTamanho(p.getRegistroConselho(), 50, "RegistroConselho");
    }
    private Usuario resolverCadastrador(Usuario informado) {
        if (informado == null) return null;
        if (informado.getId() == null) throw new IllegalArgumentException("Informe o ID do usuário cadastrador.");
        return usuarioRepository.findById(informado.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário cadastrador não encontrado."));
    }

    private void validarTamanho(String valor, int limite, String campo) {
        if (valor.length() > limite) throw new IllegalArgumentException(campo + " excede o limite de " + limite + " caracteres.");
    }

    private boolean vazio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
