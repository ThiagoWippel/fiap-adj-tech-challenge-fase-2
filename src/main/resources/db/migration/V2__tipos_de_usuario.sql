-- V2: tipos de usuário (Fase 2).
--
-- A colação utf8mb4_0900_ai_ci não diferencia maiúsculas nem acentos, e as
-- restrições únicas e as buscas por nome seguem essa regra. Diferente da
-- utf8mb4_unicode_ci, ela distingue um emoji de outro ("Pizza 🍕" e "Pizza 🍔") e
-- considera o espaço no fim. A aplicação tira os espaços das pontas antes de gravar
-- e de buscar, e as colunas únicas de texto têm um CHECK que exige o valor aparado:
-- sem ele, "Lasanha " escaparia da restrição única de "Lasanha".

CREATE TABLE tipo_usuario (
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    nome    VARCHAR(50) NOT NULL,
    -- Gerado do nome na criação e nunca alterado
    codigo  VARCHAR(50) NOT NULL,

    CONSTRAINT pk_tipo_usuario        PRIMARY KEY (id),
    CONSTRAINT uk_tipo_usuario_nome   UNIQUE (nome),
    CONSTRAINT uk_tipo_usuario_codigo UNIQUE (codigo),
    CONSTRAINT ck_tipo_usuario_nome_aparado CHECK (nome = TRIM(nome)),
    CONSTRAINT ck_tipo_usuario_codigo_aparado CHECK (codigo = TRIM(codigo))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- Tipos de sistema: podem ser renomeados, não excluídos
INSERT INTO tipo_usuario (nome, codigo) VALUES
    ('Cliente', 'CLIENTE'),
    ('Dono de Restaurante', 'DONO_RESTAURANTE');
