-- Schema do banco - Tech Challenge Fase 2
--
-- O MySQL executa este script na primeira inicialização do contêiner, com o
-- volume vazio. Os testes de integração usam o mesmo script. O Hibernate não
-- cria tabelas, só confere o mapeamento.
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

CREATE TABLE restaurante (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    nome                   VARCHAR(120) NOT NULL,
    -- Nome da constante do enum TipoCozinha, como ITALIANA
    tipo_cozinha           VARCHAR(30)  NOT NULL,

    endereco_rua           VARCHAR(150) NOT NULL,
    endereco_numero        VARCHAR(10)  NOT NULL,
    endereco_complemento   VARCHAR(60)  NULL,
    endereco_bairro        VARCHAR(80)  NOT NULL,
    endereco_cidade        VARCHAR(80)  NOT NULL,
    endereco_estado        VARCHAR(2)   NOT NULL,
    endereco_cep           VARCHAR(8)   NOT NULL,

    dono_id                BIGINT       NOT NULL,
    data_criacao           DATETIME(6)  NOT NULL,
    data_ultima_alteracao  DATETIME(6)  NOT NULL,
    -- Exclusão lógica: pedidos e avaliações das próximas fases vão apontar para o restaurante
    removido_em            DATETIME(6)  NULL,

    CONSTRAINT pk_restaurante      PRIMARY KEY (id),
    -- Usuários nunca são apagados de verdade; a chave protege o histórico
    CONSTRAINT fk_restaurante_dono FOREIGN KEY (dono_id) REFERENCES usuario (id),

    -- Restaurantes por dono e a verificação de restaurante ativo
    INDEX idx_restaurante_dono (dono_id),
    -- Filtro por tipo de cozinha, por igualdade
    INDEX idx_restaurante_cozinha (tipo_cozinha)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- Turnos de funcionamento. Vários por dia; a sobreposição é verificada no domínio.
-- Um turno com fechamento antes da abertura termina no dia seguinte.
CREATE TABLE horario_funcionamento (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    restaurante_id  BIGINT      NOT NULL,
    -- SEGUNDA, TERCA, ..., DOMINGO
    dia_semana      VARCHAR(10) NOT NULL,
    abertura        TIME        NOT NULL,
    fechamento      TIME        NOT NULL,

    CONSTRAINT pk_horario_funcionamento PRIMARY KEY (id),
    -- O turno faz parte do restaurante; no dia a dia, os turnos saem pelo PUT, removidos pelo JPA
    CONSTRAINT fk_horario_restaurante FOREIGN KEY (restaurante_id) REFERENCES restaurante (id) ON DELETE CASCADE,
    -- O domínio já confere; o banco recusa o turno impossível mesmo vindo de fora da aplicação
    CONSTRAINT ck_horario_dia_semana CHECK (dia_semana IN ('SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA',
                                                           'SABADO', 'DOMINGO')),
    CONSTRAINT ck_horario_abertura_fechamento CHECK (abertura <> fechamento),

    INDEX idx_horario_restaurante (restaurante_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE item_cardapio (
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    restaurante_id         BIGINT        NOT NULL,
    nome                   VARCHAR(100)  NOT NULL,
    descricao              VARCHAR(500)  NOT NULL,
    preco                  DECIMAL(10,2) NOT NULL,
    -- Disponível só para consumo no restaurante; sem valor padrão
    apenas_no_local        BOOLEAN       NOT NULL,
    -- Só o caminho da foto: o enunciado dispensa o upload
    caminho_foto           VARCHAR(255)  NOT NULL,
    data_criacao           DATETIME(6)   NOT NULL,
    data_ultima_alteracao  DATETIME(6)   NOT NULL,
    removido_em            DATETIME(6)   NULL,
    -- 1 enquanto o item está ativo, nulo depois de removido. Como o MySQL trata nulos
    -- como diferentes, a restrição única abaixo só vale entre os itens ativos.
    ativo_marcador         TINYINT       GENERATED ALWAYS AS (IF(removido_em IS NULL, 1, NULL)) STORED,

    CONSTRAINT pk_item_cardapio        PRIMARY KEY (id),
    -- Restaurantes nunca são apagados de verdade; a chave protege o histórico
    CONSTRAINT fk_item_restaurante     FOREIGN KEY (restaurante_id) REFERENCES restaurante (id),
    -- Começa por restaurante_id, então também atende a listagem do cardápio
    CONSTRAINT uk_item_nome_ativo      UNIQUE (restaurante_id, nome, ativo_marcador),
    CONSTRAINT ck_item_nome_aparado    CHECK (nome = TRIM(nome)),
    CONSTRAINT ck_item_preco_positivo  CHECK (preco > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
