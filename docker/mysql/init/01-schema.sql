-- =============================================================
-- Estrutura do banco de dados - Tech Challenge Fase 1
--
-- Executado automaticamente pelo MySQL na PRIMEIRA inicialização
-- do contêiner, quando o volume de dados ainda esta vazio.
--
-- A criação do esquema é responsabilidade do banco, não da aplicação.
-- No perfil docker o Hibernate opera em modo "validate": confere se a
-- estrutura corresponde ao mapeamento das entidades, sem criá-la nem
-- alterá-la. Assim a estrutura e um artefato explícito e versionado,
-- e não efeito colateral do mapeamento objeto-relacional.
-- =============================================================

CREATE TABLE IF NOT EXISTS usuario (

    id                     BIGINT       NOT NULL AUTO_INCREMENT,

    -- Coluna discriminadora da heranca em tabela unica.
    -- Valores possiveis: CLIENTE, DONO_RESTAURANTE.
    tipo_usuario           VARCHAR(20)  NOT NULL,

    -- ----- Dados comuns a todos os usuarios -----
    nome                   VARCHAR(120) NOT NULL,
    -- 255 acomoda o limite normativo de 254 caracteres de um e-mail.
    email                  VARCHAR(255) NOT NULL,
    login                  VARCHAR(50)  NOT NULL,
    -- Hash BCrypt (60 caracteres fixos). A folga evita acoplar o
    -- esquema a um algoritmo de hash especifico.
    senha                  VARCHAR(100) NOT NULL,

    -- ----- Documentos especificos por subtipo -----
    -- Aceitam nulo por consequencia da estrategia de tabela unica: o
    -- registro de um cliente nao preenche o CNPJ, e vice-versa. A
    -- obrigatoriedade por tipo e garantida na camada de aplicacao.
    -- Armazenados apenas com digitos, sem pontuacao.
    cpf                    VARCHAR(11)  NULL,
    cnpj                   VARCHAR(14)  NULL,

    -- ----- Auditoria -----
    -- DATETIME(6) preserva a precisao de microssegundos do java.time.
    data_criacao           DATETIME(6)  NOT NULL,
    data_ultima_alteracao  DATETIME(6)  NOT NULL,

    -- ----- Endereco (objeto de valor embutido) -----
    endereco_rua           VARCHAR(150) NOT NULL,
    -- Texto: existem valores como "S/N" e "123-A".
    endereco_numero        VARCHAR(10)  NOT NULL,
    endereco_complemento   VARCHAR(60)  NULL,
    endereco_bairro        VARCHAR(80)  NOT NULL,
    endereco_cidade        VARCHAR(80)  NOT NULL,
    endereco_estado        VARCHAR(2)   NOT NULL,
    endereco_cep           VARCHAR(9)   NOT NULL,

    CONSTRAINT pk_usuario  PRIMARY KEY (id),

    -- Unicidade garantida no banco, unico lugar capaz de resolver
    -- requisicoes concorrentes. A verificacao na camada de servico
    -- existe para produzir mensagem de erro legivel, nao para
    -- substituir estas restricoes.
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT uk_usuario_cpf   UNIQUE (cpf),
    CONSTRAINT uk_usuario_cnpj  UNIQUE (cnpj)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Filtragem por tipo de usuario e operacao esperada nas proximas fases.
CREATE INDEX idx_usuario_tipo ON usuario (tipo_usuario);
