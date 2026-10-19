-- V1: estrutura da Fase 1, igual à do docker/mysql/init/01-schema.sql da tag fase-1-entregue.
-- Um banco criado pela Fase 1 já está nesta versão: sem o histórico do Flyway, o baseline o
-- reconhece como V1 e as migrações seguem da V2.

CREATE TABLE IF NOT EXISTS usuario (

    id                     BIGINT       NOT NULL AUTO_INCREMENT,

    -- Coluna discriminadora da herança em tabela única.
    -- Valores possíveis: CLIENTE, DONO_RESTAURANTE.
    tipo_usuario           VARCHAR(20)  NOT NULL,

    -- ----- Dados comuns a todos os usuários -----
    nome                   VARCHAR(120) NOT NULL,
    -- 255 acomoda o limite normativo de 254 caracteres de um e-mail.
    email                  VARCHAR(255) NOT NULL,
    login                  VARCHAR(50)  NOT NULL,
    -- Hash BCrypt (60 caracteres fixos). A folga evita acoplar o
    -- esquema a um algoritmo de hash específico.
    senha                  VARCHAR(100) NOT NULL,

    -- ----- Documentos específicos por subtipo -----
    -- Aceitam nulo por consequência da estratégia de tabela única: o
    -- registro de um cliente não preenche o CNPJ, e vice-versa. A
    -- obrigatoriedade por tipo é garantida na camada de aplicação.
    -- Armazenados apenas com dígitos, sem pontuação.
    cpf                    VARCHAR(11)  NULL,
    cnpj                   VARCHAR(14)  NULL,

    -- ----- Auditoria -----
    -- DATETIME(6) preserva a precisão de microssegundos do java.time.
    data_criacao           DATETIME(6)  NOT NULL,
    data_ultima_alteracao  DATETIME(6)  NOT NULL,

    -- ----- Endereço (objeto de valor embutido) -----
    endereco_rua           VARCHAR(150) NOT NULL,
    -- Texto: existem valores como "S/N" e "123-A".
    endereco_numero        VARCHAR(10)  NOT NULL,
    endereco_complemento   VARCHAR(60)  NULL,
    endereco_bairro        VARCHAR(80)  NOT NULL,
    endereco_cidade        VARCHAR(80)  NOT NULL,
    endereco_estado        VARCHAR(2)   NOT NULL,
    endereco_cep           VARCHAR(9)   NOT NULL,

    CONSTRAINT pk_usuario  PRIMARY KEY (id),

    -- Unicidade garantida no banco, único lugar capaz de resolver
    -- requisições concorrentes. A verificação na camada de serviço
    -- existe para produzir mensagem de erro legível, não para
    -- substituir estas restrições.
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT uk_usuario_cpf   UNIQUE (cpf),
    CONSTRAINT uk_usuario_cnpj  UNIQUE (cnpj)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Filtragem por tipo de usuário e operação esperada nas próximas fases.
CREATE INDEX idx_usuario_tipo ON usuario (tipo_usuario);
