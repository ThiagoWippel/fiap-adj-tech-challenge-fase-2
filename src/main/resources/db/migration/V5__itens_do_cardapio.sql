-- V5: itens do cardápio (Fase 2).

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
