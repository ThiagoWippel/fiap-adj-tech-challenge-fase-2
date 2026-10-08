-- Schema do banco - Tech Challenge Fase 2
--
-- O MySQL executa este script na primeira inicialização do contêiner, com o
-- volume vazio. Os testes de integração usam o mesmo script. O Hibernate não
-- cria tabelas, só confere o mapeamento.
--
-- A colação utf8mb4_unicode_ci não diferencia maiúsculas, acentos nem espaço no
-- fim. As restrições únicas e as buscas por nome seguem essa regra.

CREATE TABLE tipo_usuario (
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    nome    VARCHAR(50) NOT NULL,
    -- Gerado do nome na criação e nunca alterado
    codigo  VARCHAR(50) NOT NULL,

    CONSTRAINT pk_tipo_usuario        PRIMARY KEY (id),
    CONSTRAINT uk_tipo_usuario_nome   UNIQUE (nome),
    CONSTRAINT uk_tipo_usuario_codigo UNIQUE (codigo)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Tipos de sistema: podem ser renomeados, não excluídos
INSERT INTO tipo_usuario (nome, codigo) VALUES
    ('Cliente', 'CLIENTE'),
    ('Dono de Restaurante', 'DONO_RESTAURANTE');

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
  COLLATE = utf8mb4_unicode_ci;
