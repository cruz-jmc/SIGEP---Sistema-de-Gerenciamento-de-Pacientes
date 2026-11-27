ALTER TABLE profissional
    ADD COLUMN especialidade_id BIGINT;

-- Migra eventuais vínculos existentes antes de remover a tabela intermediária
UPDATE profissional p
SET especialidade_id = (
    SELECT pe.especialidade_id
    FROM profissional_especialidade pe
    WHERE pe.profissional_id = p.id
    LIMIT 1
    );

ALTER TABLE profissional
    ALTER COLUMN especialidade_id SET NOT NULL,
    ADD CONSTRAINT fk_profissional_especialidade
        FOREIGN KEY (especialidade_id) REFERENCES especialidade (id);

-- Garante unicidade combinada de conselho + registro conforme cronograma
ALTER TABLE profissional
DROP CONSTRAINT IF EXISTS uk_profissional_registro,
    ADD CONSTRAINT uk_profissional_conselho_registro UNIQUE (conselho, registro_conselho);

DROP TABLE IF EXISTS profissional_especialidade;