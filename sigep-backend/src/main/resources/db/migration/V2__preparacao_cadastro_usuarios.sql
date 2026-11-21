-- Permitir funcionários sem senha antes da ativação
ALTER TABLE usuario
    ALTER COLUMN senha_hash DROP NOT NULL;

-- Garantir apenas um coordenador no sistema
CREATE UNIQUE INDEX uq_usuario_coordenador_unico
    ON usuario (perfil)
    WHERE perfil = 'COORDENADOR';
