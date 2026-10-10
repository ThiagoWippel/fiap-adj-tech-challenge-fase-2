# EXPLAIN das consultas principais

MySQL 8.4.11, com o schema de `docker/mysql/init/01-schema.sql` e uma massa de 3.000 usuários, 6.000 restaurantes, 12.000 turnos e 60.000 itens (parte deles removida).

Cada consulta aparece duas vezes: **sem índice**, forçada com `IGNORE INDEX` (o MySQL não deixa apagar um índice que sustenta uma chave estrangeira), e **com índice**, como a aplicação roda. A coluna `rows` é a estimativa de linhas que o MySQL precisa ler; `type = ALL` é leitura da tabela inteira.

Para gerar de novo: `python3 ferramentas/gerar_explain.py` (fora do repositório, junto do relatório).

## Restaurantes de um dono

Listagem de `GET /usuarios/{id}/restaurantes` e a contagem que bloqueia a exclusão e a troca de tipo do dono (EXC-07, TRO-10).

```sql
SELECT * FROM restaurante WHERE dono_id = 252 AND removido_em IS NULL ORDER BY nome LIMIT 10
```

| Situação | type | key | rows | filtered | Extra |
|---|---|---|---|---|---|
| sem índice | ALL | NULL | 5856 | 0.02 | Using where; Using filesort |
| com `idx_restaurante_dono` | ref | idx_restaurante_dono | 12 | 10.00 | Using where; Using filesort |

## Restaurantes por tipo de cozinha

Filtro `tipoCozinha` da listagem de restaurantes (COZ-03).

```sql
SELECT * FROM restaurante WHERE nome LIKE '%1%' AND tipo_cozinha = 'ITALIANA' AND removido_em IS NULL ORDER BY nome LIMIT 10
```

| Situação | type | key | rows | filtered | Extra |
|---|---|---|---|---|---|
| sem índice | ALL | NULL | 5856 | 0.06 | Using where; Using filesort |
| com `idx_restaurante_cozinha` | ref | idx_restaurante_cozinha | 334 | 1.11 | Using where; Using filesort |

## Turnos dos restaurantes de uma página

Os turnos de todos os restaurantes da página vêm numa consulta só (`@BatchSize`, RES-18).

```sql
SELECT * FROM horario_funcionamento WHERE restaurante_id IN (11, 12, 13, 14, 15, 16, 17, 18, 19, 21)
```

| Situação | type | key | rows | filtered | Extra |
|---|---|---|---|---|---|
| sem índice | ALL | NULL | 11491 | 50.00 | Using where |
| com `idx_horario_restaurante` | range | idx_horario_restaurante | 20 | 100.00 | Using index condition |

## Usuários de um tipo

Contagem que bloqueia a exclusão de tipo em uso (TIP-11) e listagem de `GET /tipos-usuario/{id}/usuarios`.

```sql
SELECT COUNT(*) FROM usuario WHERE tipo_usuario_id = 3 AND removido_em IS NULL
```

| Situação | type | key | rows | filtered | Extra |
|---|---|---|---|---|---|
| sem índice | ALL | NULL | 3006 | 1.67 | Using where |
| com `idx_usuario_tipo` | ref | idx_usuario_tipo | 300 | 10.00 | Using where |

## Cardápio de um restaurante

Listagem de itens (ITE-21). Usa o índice único do nome, que começa por `restaurante_id`: não precisou de índice extra.

```sql
SELECT * FROM item_cardapio WHERE restaurante_id = 42 AND removido_em IS NULL ORDER BY nome LIMIT 10
```

| Situação | type | key | rows | filtered | Extra |
|---|---|---|---|---|---|
| sem índice | ALL | NULL | 59736 | 0.00 | Using where; Using filesort |
| com `uk_item_nome_ativo` | ref | uk_item_nome_ativo | 10 | 10.00 | Using where |

## Nome de item já usado no restaurante

Verificação de unicidade antes de gravar o item (ITE-12).

```sql
SELECT id FROM item_cardapio WHERE restaurante_id = 42 AND nome = 'Prato 3' AND removido_em IS NULL LIMIT 1
```

| Situação | type | key | rows | filtered | Extra |
|---|---|---|---|---|---|
| sem índice | ALL | NULL | 59736 | 0.00 | Using where |
| com `uk_item_nome_ativo` | ref | uk_item_nome_ativo | 1 | 10.00 | Using where |

## Busca de usuários pelo trecho do nome

`GET /usuarios?nome=` e a v2. Um `LIKE` com `%` no começo não aproveita índice de árvore, então não há índice em `nome`: com ou sem ele, o MySQL leria a tabela inteira.

```sql
SELECT * FROM usuario WHERE nome LIKE '%silva%' AND removido_em IS NULL ORDER BY nome LIMIT 10
```

| Situação | type | key | rows | filtered | Extra |
|---|---|---|---|---|---|
| como roda | ALL | NULL | 3006 | 1.11 | Using where; Using filesort |
