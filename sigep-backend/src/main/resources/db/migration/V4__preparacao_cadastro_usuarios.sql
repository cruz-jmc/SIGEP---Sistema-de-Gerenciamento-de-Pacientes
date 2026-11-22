-- Impede gravações concorrentes entre as verificações e a criação dos índices.
LOCK TABLE usuario IN SHARE ROW EXCLUSIVE MODE;

DO $$
BEGIN
    IF (SELECT count(*) FROM usuario WHERE perfil = 'COORDENADOR') > 1 THEN
        RAISE EXCEPTION 'V4: existe mais de um usuário com perfil COORDENADOR.'
            USING HINT = 'Revise os perfis existentes antes de aplicar a migração.';
    END IF;

    IF EXISTS (SELECT 1 FROM usuario GROUP BY lower(email) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'V4: existem usuários com e-mails duplicados após lower(email).'
            USING HINT = 'Resolva as colisões de e-mail antes de aplicar a migração.';
    END IF;
END
$$;

-- A coluna legada da V1 permanece para preservar dados existentes.
ALTER TABLE usuario ALTER COLUMN primeiro_acesso SET DEFAULT false;
ALTER TABLE usuario ADD COLUMN revisao_credenciais BIGINT NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX uq_usuario_coordenador_unico
    ON usuario (perfil)
    WHERE perfil = 'COORDENADOR';

CREATE UNIQUE INDEX uq_usuario_email_normalizado
    ON usuario (lower(email));
