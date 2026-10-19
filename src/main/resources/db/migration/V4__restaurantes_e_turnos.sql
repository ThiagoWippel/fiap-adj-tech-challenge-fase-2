-- V4: restaurantes e turnos de funcionamento (Fase 2).

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
