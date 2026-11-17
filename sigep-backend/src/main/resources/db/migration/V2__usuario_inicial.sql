INSERT INTO usuario (nome, email, senha_hash, perfil, ativo, primeiro_acesso, criado_em)
VALUES ('funcionario Teste', 'admin@sigep.upe.br', '123456', 'FUNCIONARIO', true, false, NOW())
ON CONFLICT (email) DO NOTHING;