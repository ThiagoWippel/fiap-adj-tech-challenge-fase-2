-- V3: o usuário da Fase 2. O tipo passa a ser um registro de tipo_usuario, CPF e CNPJ viram um
-- só documento, e os dados pessoais aceitam nulo para a anonimização.
--
-- A tabela da Fase 1 é renomeada, a nova nasce vazia e recebe os dados convertidos. A antiga só sai
-- no fim: o MySQL não desfaz DDL, e uma falha no meio deixa os dados em usuario_fase_1.
RENAME TABLE usuario TO usuario_fase_1;

CREATE TABLE usuario (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    nome                   VARCHAR(120) NOT NULL,

    -- Os campos pessoais aceitam nulo só para a anonimização (ver a regra CHECK)
    email                  VARCHAR(255) NULL,
    login                  VARCHAR(50)  NULL,
    -- Hash BCrypt, com folga para outro algoritmo
    senha                  VARCHAR(100) NULL,
    -- CPF (11 dígitos) ou CNPJ (14), só com dígitos
    documento              VARCHAR(14)  NULL,
    tipo_usuario_id        BIGINT       NULL,

    endereco_rua           VARCHAR(150) NULL,
    endereco_numero        VARCHAR(10)  NULL,
    endereco_complemento   VARCHAR(60)  NULL,
    endereco_bairro        VARCHAR(80)  NULL,
    endereco_cidade        VARCHAR(80)  NULL,
    endereco_estado        VARCHAR(2)   NULL,
    endereco_cep           VARCHAR(8)   NULL,

    data_criacao           DATETIME(6)  NOT NULL,
    data_ultima_alteracao  DATETIME(6)  NOT NULL,
    removido_em            DATETIME(6)  NULL,

    CONSTRAINT pk_usuario           PRIMARY KEY (id),
    -- O MySQL aceita vários nulos numa coluna única: usuários anonimizados não colidem
    CONSTRAINT uk_usuario_email     UNIQUE (email),
    CONSTRAINT uk_usuario_login     UNIQUE (login),
    CONSTRAINT uk_usuario_documento UNIQUE (documento),
    CONSTRAINT ck_usuario_email_aparado CHECK (email = TRIM(email)),
    CONSTRAINT ck_usuario_login_aparado CHECK (login = TRIM(login)),
    CONSTRAINT ck_usuario_documento_aparado CHECK (documento = TRIM(documento)),
    -- Sem ON DELETE: o MySQL não aceita ação referencial em coluna usada num CHECK.
    -- O padrão do InnoDB já impede excluir um tipo em uso.
    CONSTRAINT fk_usuario_tipo_usuario FOREIGN KEY (tipo_usuario_id) REFERENCES tipo_usuario (id),
    -- Usuário ativo tem todos os dados preenchidos
    CONSTRAINT ck_usuario_ativo_completo CHECK (
        removido_em IS NOT NULL OR (
            email IS NOT NULL AND login IS NOT NULL AND senha IS NOT NULL
            AND documento IS NOT NULL AND tipo_usuario_id IS NOT NULL
            AND endereco_rua IS NOT NULL AND endereco_numero IS NOT NULL
            AND endereco_bairro IS NOT NULL AND endereco_cidade IS NOT NULL
            AND endereco_estado IS NOT NULL AND endereco_cep IS NOT NULL
        )
    ),

    -- Usuários por tipo, e a verificação de tipo em uso
    INDEX idx_usuario_tipo (tipo_usuario_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


-- O discriminador vira o tipo pelo código, CPF ou CNPJ vira o documento, o CEP perde o hífen que a
-- Fase 1 aceitava e os textos únicos perdem os espaços das pontas. O LEFT JOIN não descarta usuário:
-- um discriminador desconhecido deixa o tipo nulo, e o CHECK de usuário ativo recusa a cópia.
INSERT INTO usuario (id, nome, email, login, senha, documento, tipo_usuario_id,
                     endereco_rua, endereco_numero, endereco_complemento, endereco_bairro,
                     endereco_cidade, endereco_estado, endereco_cep,
                     data_criacao, data_ultima_alteracao)
SELECT f.id, f.nome, TRIM(f.email), TRIM(f.login), f.senha, TRIM(COALESCE(f.cpf, f.cnpj)), t.id,
       f.endereco_rua, f.endereco_numero, f.endereco_complemento, f.endereco_bairro,
       f.endereco_cidade, f.endereco_estado, REPLACE(f.endereco_cep, '-', ''),
       f.data_criacao, f.data_ultima_alteracao
FROM usuario_fase_1 f
LEFT JOIN tipo_usuario t ON t.codigo = f.tipo_usuario COLLATE utf8mb4_0900_ai_ci;

DROP TABLE usuario_fase_1;
