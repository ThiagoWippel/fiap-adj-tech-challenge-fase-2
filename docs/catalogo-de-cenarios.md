# Catálogo de cenários

Tech Challenge · Fase 2 · sistema de gestão de restaurantes
Versão de 07/10/2026. Fontes: enunciado da Fase 2, requisitos herdados da Fase 1, feedback da Fase 1 e o dossiê de decisões.

Este catálogo lista, antes de qualquer código, tudo o que o sistema precisa comprovar. Cada cenário tem um ID estável, e o mesmo ID aparece em três lugares:

- no `@DisplayName` do teste que o comprova, por exemplo `TIP-11 · deve recusar a exclusão de tipo em uso`;
- na descrição da requisição da collection do Postman, quando o cenário também é verificado por ela;
- na matriz requisito → evidência do relatório.

Assim qualquer frase do enunciado leva a um cenário, e cada cenário leva a um teste que roda.

## Como ler

**Nível** diz onde o cenário é comprovado:

| Nível | Significa | Ferramenta |
|---|---|---|
| U | Unitário de domínio: entidade ou objeto de valor, sem dependências | JUnit 5 + AssertJ |
| C | Unitário de caso de uso, com gateways e portas substituídos por mocks | JUnit 5 + Mockito |
| I | Integração contra MySQL real em container | Testcontainers + `@SpringBootTest` / `@DataJpaTest` + REST-Assured |
| P | Collection do Postman, rodada pelo Newman contra o docker-compose | Postman + Newman |
| A | Regra de arquitetura | ArchUnit |
| CI | Verificação feita pelo pipeline do GitHub Actions | GitHub Actions |

**Origem** diz de onde o cenário vem:

| Código | Origem |
|---|---|
| EN-OBJ | Enunciado, seção "Objetivo" ("expande o sistema", "código limpo", "execução integrada") |
| EN-TU | Enunciado, seção "Tipo de usuário" |
| EN-RE | Enunciado, seção "Cadastro de restaurante" |
| EN-IT | Enunciado, seção "Cadastro dos itens do cardápio" |
| EN-1 a EN-9 | Enunciado, entregáveis e fatores de avaliação 1 a 9 |
| F1-01 a F1-13 | Requisitos herdados da Fase 1 (lista abaixo) |
| FB-1 a FB-3 | Feedback da Fase 1: login sem testes e geração de tokens; busca por nome na collection; ProblemDetail |
| Dec. A a X | Decisão do dossiê com a letra correspondente |

Requisitos herdados da Fase 1:

| Código | Requisito |
|---|---|
| F1-01 | Cadastro, atualização e exclusão de usuários |
| F1-02 | Troca de senha em endpoint separado |
| F1-03 | Atualização dos demais dados em endpoint distinto do de senha |
| F1-04 | Registro da data da última alteração |
| F1-05 | Busca de usuários pelo nome |
| F1-06 | Unicidade do e-mail |
| F1-07 | Serviço de validação de login |
| F1-08 | Os dois tipos de usuário obrigatórios |
| F1-09 | Campos obrigatórios: nome, e-mail, login, senha, data da última alteração e endereço |
| F1-10 | Estratégia de versionamento de API |
| F1-11 | Respostas de erro no padrão ProblemDetail |
| F1-12 | Swagger com exemplos de sucesso e de erro |
| F1-13 | Banco relacional em container |

Decisões do dossiê: A repositório · B token · C horário · D tipo do dono · E exclusão · F banco · G transação · H auditoria · I pacotes · J documento · K Lombok e records · L gateway e data source · M rotas do cardápio · N cobertura · O exceção de domínio · P versionamento · Q contrato da Fase 1 · R tipo de cozinha · S nome do item · T banco nos testes · U método · V extras · W campos e listagens · X textos com acento.

## Resumo

| Fatia | Tema | Cenários |
|---|---|---|
| 0 | Fundação: arquitetura, infraestrutura, erros, paginação | 27 |
| 1 | Usuário: herança da Fase 1, login, token, exclusão | 59 |
| 2 | Tipo de usuário e troca de tipo | 27 |
| 3 | Restaurante, horários e tipo de cozinha | 38 |
| 4 | Item do cardápio | 22 |
| | **Total** | **173** |

Os cenários de paginação (PAG) valem para todas as listagens e são repetidos no teste de integração de cada uma.

---

## Fatia 0 · Fundação

### Arquitetura (ARQ)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| ARQ-01 | Os pacotes `domain` e `application` não dependem de framework: Spring, Jakarta, Hibernate nem Jackson. | A | EN-7, Dec. I |
| ARQ-02 | Os pacotes `domain`, `application` e `interfaceadapter` não dependem de `infrastructure`. | A | EN-7 |
| ARQ-03 | As camadas respeitam a direção das dependências: infraestrutura → adaptadores → aplicação → domínio. | A | EN-7 |
| ARQ-04 | O pacote `interfaceadapter` não usa classes nem anotações do Spring: controllers, gateways e presenters são Java puro. | A | EN-7, Dec. L |

### Infraestrutura (INF)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| INF-01 | Dado o MySQL do Testcontainers criado pelo script de schema, quando o contexto sobe, então o Hibernate em modo `validate` aceita o mapeamento de todas as entidades. | I | EN-8, Dec. T |
| INF-02 | Quando consulto `/actuator/health` com o banco no ar, então recebo `UP`. | I | EN-5, Dec. V |
| INF-03 | Dado um clone limpo e o `.env` copiado do `.env.example`, quando rodo `docker compose up`, então aplicação e banco sobem e o healthcheck da aplicação passa. | CI | EN-5, EN-OBJ |
| INF-04 | Quando a cobertura dos testes unitários no núcleo (`domain`, `application`, `interfaceadapter`) ou a cobertura total cai abaixo de 80% de linha ou de ramo, então o build falha. Testado de propósito uma vez. | CI | EN-8, Dec. N |
| INF-05 | Quando consulto `/v3/api-docs`, então os 26 endpoints aparecem, cada um com ao menos um exemplo de sucesso e um de erro. | I | F1-12, EN-3 |
| INF-06 | Dado o perfil `docker` sem a chave de assinatura do token, quando a aplicação sobe, então ela falha na inicialização com uma mensagem que nomeia a variável de ambiente que falta. | I | Dec. B |
| INF-07 | A cada push, o GitHub Actions roda `mvn verify` (unitários, integração com Testcontainers, ArchUnit e os cortes de cobertura) e termina verde. | CI | EN-6, EN-8, Dec. V |

### Erros (ERR)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| ERR-01 | Quando qualquer erro acontece, então a resposta é `application/problem+json` com `type`, `title`, `status`, `detail`, `instance` e `momento`. | I | F1-11, FB-3 |
| ERR-02 | O `type` aponta para o namespace estável da própria aplicação (`/problemas/{tipo}`), e essa URL responde 200 com a descrição do problema. | I | FB-3 |
| ERR-03 | Dado um corpo que falha no Bean Validation, então a resposta traz a extensão `erros` com o campo e a mensagem de cada falha. | I | F1-11 |
| ERR-04 | Dado um JSON malformado, então a resposta é 400 em ProblemDetail. | I | F1-11 |
| ERR-05 | Dado um verbo HTTP não suportado na rota, então a resposta é 405 em ProblemDetail. | I | F1-11 |
| ERR-06 | Dada uma rota inexistente, então a resposta é 404 em ProblemDetail. | I | F1-11 |
| ERR-07 | Dada uma falha inesperada, então a resposta é 500 sem rastro de pilha no corpo; o rastro vai só para o log. | I | F1-11 |
| ERR-08 | `ValidacaoDeDominioException` é uma `IllegalArgumentException` e vira 400; uma `IllegalArgumentException` lançada fora do domínio vira 500. | U · I | Dec. O |
| ERR-09 | Títulos e mensagens saem em português com acento, por exemplo "Regra de negócio violada". | I | Dec. X |
| ERR-10 | A extensão `momento` é preenchida num único lugar e aparece em todos os erros, inclusive nos gerados pelo próprio Spring (400, 404 e 405). | I | FB-3 |

### Paginação (PAG)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| PAG-01 | Dada uma listagem sem parâmetros, então vêm 10 itens por página, ordenados por nome, no formato `conteudo` mais os metadados de paginação. | I | Dec. W |
| PAG-02 | Dado `size` acima de 50, então a página vem com no máximo 50 itens e os metadados mostram o tamanho efetivo. | I | Dec. W |
| PAG-03 | Dado `sort` por um campo que não existe, então a resposta é 400. | I | Dec. W |
| PAG-04 | Dada uma página além da última, então a resposta é 200 com conteúdo vazio. | I | Dec. W |
| PAG-05 | Registros removidos nunca aparecem em nenhuma listagem. | I | Dec. E |
| PAG-06 | A contagem de elementos nos metadados considera só registros ativos. | I | Dec. E |

---

## Fatia 1 · Usuário

### Endereço (END)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| END-01 | Endereço com rua, número, bairro, cidade, UF e CEP é aceito; o complemento é opcional. | U | F1-09 |
| END-02 | O número aceita valores como "S/N" e "123-A". | U | F1-09 |
| END-03 | UF fora das 27 siglas brasileiras é recusada. | U | F1-09 |
| END-04 | CEP sem 8 dígitos é recusado; CEP com hífen é aceito e guardado só com dígitos. | U | F1-09 |
| END-05 | Rua, número, bairro, cidade, UF ou CEP ausente é recusado. | U | F1-09 |

### Documento (DOC)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| DOC-01 | CPF com dígitos verificadores válidos é aceito; com máscara, é aceito e guardado só com dígitos. | U | Dec. J |
| DOC-02 | CPF com dígitos verificadores inválidos, ou com todos os dígitos iguais, é recusado. | U | Dec. J |
| DOC-03 | CNPJ válido é aceito; com máscara, é aceito e guardado só com dígitos. | U | Dec. J |
| DOC-04 | CNPJ com dígitos verificadores inválidos, ou com todos os dígitos iguais, é recusado. | U | Dec. J |
| DOC-05 | Usuário do tipo Dono de Restaurante exige CNPJ; com CPF, é recusado. | U | Dec. D, J |
| DOC-06 | Usuário de qualquer outro tipo exige CPF; com CNPJ, é recusado. | U | Dec. D, J |

### Usuário (USU)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| USU-01 | Usuário válido é criado com nome, e-mail, login, senha já codificada, endereço, tipo e documento. | U | F1-09 |
| USU-02 | Nome vazio ou fora de 3 a 120 caracteres é recusado. | U | F1-09 |
| USU-03 | E-mail em formato inválido é recusado. | U | F1-09 |
| USU-04 | Login fora de 4 a 50 caracteres, ou com caractere que não seja letra, número, ponto, hífen ou sublinhado, é recusado. | U | F1-09 |
| USU-05 | Alterar o nome para um valor inválido falha, e o usuário continua com o nome anterior. | U | Dec. K |
| USU-06 | No cadastro válido, a senha passa pela porta de codificação e nunca é gravada em texto. | C | F1-01, FB-1 |
| USU-07 | Cadastro com e-mail já usado devolve conflito e não grava nada. | C | F1-06 |
| USU-08 | Cadastro com login já usado devolve conflito. | C | F1-01 |
| USU-09 | Cadastro com documento já usado devolve conflito. | C | Dec. J |
| USU-10 | Cadastro com código de tipo inexistente devolve não encontrado. | C | Dec. D |
| USU-11 | A atualização de dados altera nome, e-mail, login e endereço, sem tocar na senha nem no tipo. | C | F1-03 |
| USU-12 | Atualização com o e-mail de outro usuário devolve conflito. | C | F1-06 |
| USU-13 | Atualização mantendo o próprio e-mail é aceita. | C | F1-06 |
| USU-14 | Atualização de usuário inexistente ou removido devolve não encontrado. | C | F1-01 |
| USU-15 | Troca de senha com a senha atual correta grava a nova senha codificada. | C | F1-02 |
| USU-16 | Troca de senha com a senha atual incorreta devolve credenciais inválidas e não grava nada. | C | F1-02 |
| USU-17 | A busca por nome tira os espaços das pontas do termo; termo vazio devolve todos os usuários ativos. | C | F1-05 |
| USU-18 | POST `/api/v1/usuarios` com cliente e CPF devolve 201, cabeçalho `Location`, tipo `CLIENTE` e o documento, sem o campo senha. | I · P | F1-01, Dec. Q |
| USU-19 | POST com dono e CNPJ devolve 201 e tipo `DONO_RESTAURANTE`. | I · P | F1-08, Dec. Q |
| USU-20 | POST com e-mail duplicado devolve 409. | I · P | F1-06 |
| USU-21 | POST com `Maria@Exemplo.com` quando `maria@exemplo.com` já existe devolve 409. | I | F1-06, Dec. T |
| USU-22 | POST com campos obrigatórios ausentes devolve 400, com `erros` nomeando cada campo. | I · P | F1-09 |
| USU-23 | POST de cliente sem CPF devolve 400 com o título "Regra de negócio violada". | I · P | Dec. J, X |
| USU-24 | GET `/api/v1/usuarios/{id}` devolve 200; id inexistente devolve 404 com `instance` igual à rota chamada. | I · P | F1-01 |
| USU-25 | PUT `/api/v1/usuarios/{id}` devolve 200; `dataUltimaAlteracao` avança e `dataCriacao` não muda. | I · P | F1-03, F1-04, Dec. H |
| USU-26 | PUT `/api/v1/usuarios/{id}/senha` devolve 204; senha atual errada devolve 401; nova senha fora de 8 a 72 caracteres devolve 400. | I · P | F1-02 |
| USU-27 | GET `/api/v1/usuarios?nome=` devolve lista simples nos três casos: com resultados, sem resultados e sem filtro. | I · P | F1-05, F1-10, FB-2 |
| USU-28 | GET `/api/v2/usuarios?nome=` devolve página com `conteudo` e metadados. | I · P | F1-05, F1-10, FB-2 |
| USU-29 | Nenhuma resposta da API contém o campo senha. | I · P | F1-01 |
| USU-30 | Persistir, atualizar e reler um usuário mantém `dataCriacao` e avança `dataUltimaAlteracao` (a armadilha do update). | I | F1-04, Dec. H |

### Login e token (LOG, TOK)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| LOG-01 | Login com credenciais válidas devolve id, nome, tipo, token e validade do token. | C · I · P | F1-07, FB-1, Dec. B |
| LOG-02 | Login inexistente devolve 401 com a mesma mensagem da senha incorreta, sem revelar que o login não existe. | C · I · P | F1-07, FB-1 |
| LOG-03 | Login inexistente ainda executa uma comparação de hash, para levar o mesmo tempo que uma senha errada. | C | F1-07 |
| LOG-04 | Senha incorreta devolve 401. | C · I | F1-07, FB-1 |
| LOG-05 | A senha antiga deixa de funcionar depois da troca. | I · P | F1-02, F1-07 |
| LOG-06 | Usuário removido não consegue fazer login. | C | Dec. E |
| LOG-07 | Login com corpo inválido (login ou senha ausente) devolve 400. | I | F1-07 |
| TOK-01 | O token emitido é um JWT assinado, com `sub` igual ao id do usuário, o código do tipo e a expiração definida na configuração. | U · I | Dec. B |
| TOK-02 | Um token adulterado não passa na verificação da assinatura. | U | Dec. B |
| TOK-03 | Nenhum endpoint exige token nesta fase: requisição sem `Authorization` é atendida normalmente. | I | Dec. B |

### Exclusão de usuário (EXC)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| EXC-01 | Excluir usuário sem restaurante ativo anonimiza o registro. | C | F1-01, Dec. E |
| EXC-02 | Excluir usuário inexistente ou já removido devolve 404. | C · I · P | F1-01, Dec. E |
| EXC-03 | Depois da exclusão, o GET devolve 404, a busca por nome não traz o usuário e o login falha. | I · P | Dec. E |
| EXC-04 | Depois da exclusão, a linha continua com id e datas; o nome vira "Usuário removido"; e-mail, login, senha, documento, endereço e tipo ficam nulos; `removido_em` é preenchido. | I | Dec. E |
| EXC-05 | Depois da exclusão, um novo cadastro com o mesmo e-mail, login e documento é aceito, com id novo. | I · P | Dec. E |
| EXC-06 | O banco recusa um usuário ativo (sem `removido_em`) com e-mail, login, senha, documento, endereço ou tipo nulo. | I | Dec. E |

### Aceitação (ACE)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| ACE-01 | A collection da Fase 1 roda contra a Fase 2 e passa inteira, com três ajustes deliberados e documentados: o `type` dos erros, os títulos com acento e a verificação de mensagem do login atualizada para as palavras acentuadas. | P · CI | EN-OBJ, Dec. Q |
| ACE-02 | A cada push, o GitHub Actions sobe o docker-compose e roda a collection completa com o Newman, e termina verde. | CI | EN-4, EN-5, Dec. V |

---

## Fatia 2 · Tipo de usuário

### Tipo (TIP)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| TIP-01 | Tipo criado com o nome "Entregador" recebe o código `ENTREGADOR`. | U | EN-TU, Dec. D |
| TIP-02 | O código é gerado sem acentos, em maiúsculas e com espaços trocados por sublinhado: "Ajudante de Cozinha" vira `AJUDANTE_DE_COZINHA`, "Gerência" vira `GERENCIA`. | U | Dec. D |
| TIP-03 | Nome vazio ou fora de 3 a 50 caracteres é recusado. | U | EN-TU |
| TIP-04 | Renomear um tipo mantém o código. | U | Dec. D |
| TIP-05 | `CLIENTE` e `DONO_RESTAURANTE` são reconhecidos como tipos de sistema pelo código. | U | EN-TU, Dec. D |
| TIP-06 | Cadastro com nome já existente, mesmo com maiúsculas ou acentos diferentes, devolve conflito. | C · I | EN-TU |
| TIP-07 | Cadastro cujo código gerado coincide com o de outro tipo devolve conflito. | C | Dec. D |
| TIP-08 | Renomear para o nome de outro tipo devolve conflito; renomear tipo inexistente devolve não encontrado. | C | EN-TU |
| TIP-09 | Renomear um tipo de sistema é permitido e não muda o código. | C | Dec. D |
| TIP-10 | Excluir tipo sem usuários ativos remove o registro. | C | EN-TU |
| TIP-11 | Excluir tipo usado por usuário ativo devolve conflito, informando quantos usuários o usam. | C · I · P | EN-TU, Dec. E |
| TIP-12 | Excluir `CLIENTE` ou `DONO_RESTAURANTE` devolve conflito. | C · I · P | EN-TU, Dec. D |
| TIP-13 | Excluir um tipo que só era usado por usuários removidos é permitido. | I | Dec. E |
| TIP-14 | Excluir tipo inexistente devolve não encontrado. | C | EN-TU |
| TIP-15 | Uma base recém-criada já tem os tipos Cliente e Dono de Restaurante, sem nenhum passo manual. | I | EN-TU |
| TIP-16 | CRUD HTTP: POST devolve 201 com o código e `Location`; GET lista paginada; GET por id 200 e 404; PUT 200, 404 e 409; DELETE 204, 404 e 409. | I · P | EN-TU, EN-1 |
| TIP-17 | GET `/api/v1/tipos-usuario/{id}/usuarios` lista, paginados, só os usuários ativos do tipo; tipo inexistente devolve 404. | I · P | EN-TU |
| TIP-18 | A chave estrangeira do banco recusa a exclusão de um tipo referenciado, mesmo se a verificação do caso de uso for contornada. | I | EN-TU, Dec. E |

### Troca de tipo (TRO)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| TRO-01 | Trocar o tipo para Dono de Restaurante com CNPJ válido troca o tipo e o documento. | U · C | EN-TU, Dec. D |
| TRO-02 | Trocar para Dono de Restaurante informando CPF é recusado. | U | Dec. D, J |
| TRO-03 | Trocar para Cliente, ou para um tipo criado pelo CRUD, informando CNPJ é recusado. | U | Dec. D, J |
| TRO-04 | Trocar com documento já usado por outro usuário devolve conflito. | C | Dec. J |
| TRO-05 | Usuário inexistente ou removido, ou tipo inexistente, devolve não encontrado. | C | EN-TU |
| TRO-06 | Dono sem restaurante ativo pode virar Cliente informando CPF. | C | Dec. D |
| TRO-07 | Trocar para o tipo atual com o mesmo documento é aceito, sem alteração. | C | Dec. D |
| TRO-08 | PATCH `/api/v1/usuarios/{id}/tipo` devolve 200 com o novo tipo e o novo documento; documento incompatível devolve 400; usuário ou tipo inexistente, 404; documento em uso, 409. | I · P | EN-TU, EN-1 |
| TRO-09 | Depois da troca, o GET do usuário e a lista de usuários do tipo mostram o novo tipo. | I · P | EN-TU |

---

## Fatia 3 · Restaurante

### Restaurante (RES)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| RES-01 | Restaurante válido é criado com nome, endereço, tipo de cozinha, ao menos um turno e dono. | U | EN-RE |
| RES-02 | Nome vazio ou fora de 2 a 120 caracteres é recusado. | U | EN-RE, Dec. W |
| RES-03 | Endereço, tipo de cozinha, horários ou dono ausente é recusado. | U | EN-RE |
| RES-04 | Alterar um campo para valor inválido falha, e o restaurante continua com o valor anterior. | U | Dec. K |
| RES-05 | Cadastro com dono do tipo Dono de Restaurante é aceito. | C | EN-RE, Dec. D |
| RES-06 | Cadastro com dono inexistente ou removido devolve não encontrado. | C | EN-RE |
| RES-07 | Cadastro com dono do tipo Cliente devolve conflito, com mensagem que orienta a trocar o tipo antes. | C · I · P | Dec. D |
| RES-08 | A atualização altera os dados e substitui os horários. | C | EN-RE |
| RES-09 | Transferir o restaurante para outro Dono de Restaurante é aceito; para um Cliente, devolve conflito. | C · I | Dec. D |
| RES-10 | Atualizar ou excluir restaurante inexistente ou removido devolve não encontrado. | C | EN-RE |
| RES-11 | Excluir um restaurante preenche `removido_em` e mantém a linha. | C · I | Dec. E |
| RES-12 | POST `/api/v1/restaurantes` devolve 201 com `Location`, o dono como `{id, nome}` e os turnos ordenados. | I · P | EN-RE, EN-1, Dec. W |
| RES-13 | GET `/api/v1/restaurantes` devolve página com filtro por nome (contém, ignorando maiúsculas) e por tipo de cozinha. | I · P | EN-RE, Dec. W |
| RES-14 | GET por id devolve 200; restaurante inexistente ou removido devolve 404. | I · P | EN-RE |
| RES-15 | PUT devolve 200; `dataCriacao` não muda e `dataUltimaAlteracao` avança. | I · P | EN-RE, Dec. H |
| RES-16 | DELETE devolve 204; depois disso, o restaurante some do GET e da listagem. | I · P | EN-RE, Dec. E |
| RES-17 | GET `/api/v1/usuarios/{id}/restaurantes` lista, paginados, os restaurantes ativos do usuário. | I · P | EN-RE |
| RES-18 | Listar restaurantes com o nome do dono executa um número fixo de consultas, sem uma consulta extra por restaurante (N+1). | I | EN-7, Dec. W |

### Horários (HOR)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| HOR-01 | Turno com dia, abertura e fechamento válidos é aceito. | U | EN-RE, Dec. C |
| HOR-02 | Turno com abertura igual ao fechamento é recusado. | U | Dec. C |
| HOR-03 | Turno com fechamento antes da abertura termina no dia seguinte e é aceito (sexta 18:00–02:00). | U | Dec. C |
| HOR-04 | Dois turnos no mesmo dia sem sobreposição são aceitos (11:00–15:00 e 18:00–23:00). | U | Dec. C |
| HOR-05 | Turnos com sobreposição parcial são recusados, com mensagem que cita os dois turnos. | U | Dec. C |
| HOR-06 | Turno contido dentro de outro é recusado. | U | Dec. C |
| HOR-07 | Turnos que encostam (11:00–15:00 e 15:00–18:00) são aceitos. | U | Dec. C |
| HOR-08 | Turno da madrugada que colide com a parte depois da meia-noite do turno da noite anterior é recusado (sexta 18:00–02:00 e sábado 01:00–10:00). | U | Dec. C |
| HOR-09 | Turno de domingo à noite que colide com segunda de madrugada é recusado (domingo 22:00–03:00 e segunda 02:00–06:00). | U | Dec. C |
| HOR-10 | Lista de turnos vazia é recusada. | U | EN-RE |
| HOR-11 | A resposta traz os turnos ordenados por dia e por horário de abertura. | U · I | Dec. C |
| HOR-12 | O PUT do restaurante substitui todos os turnos: os antigos deixam de existir no banco. | I | Dec. C |
| HOR-13 | Horário em formato inválido ("25:00", "11h") ou dia inexistente devolve 400. | I | Dec. C |

### Tipo de cozinha (COZ)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| COZ-01 | Tipo de cozinha fora da lista devolve 400, com os valores aceitos na mensagem. | U · I | EN-RE, Dec. R |
| COZ-02 | Tipo de cozinha ausente devolve 400. | I | EN-RE |
| COZ-03 | O filtro por tipo de cozinha devolve só os restaurantes daquele tipo; valor inválido no filtro devolve 400. | I · P | EN-RE, Dec. R |

### Regras que dependem de restaurante (EXC, TRO)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| EXC-07 | Excluir usuário responsável por restaurante ativo devolve 409 e não altera nada. | C · I · P | Dec. D, E |
| EXC-08 | Excluir usuário cujos restaurantes estão todos removidos é permitido, e esses restaurantes continuam apontando para o id dele. | I | Dec. E |
| TRO-10 | Dono com restaurante ativo não pode virar Cliente: devolve conflito, com orientação para transferir ou excluir o restaurante. | C · I · P | Dec. D |
| TRO-11 | Dono cujos restaurantes estão todos removidos pode virar Cliente. | C | Dec. D, E |

---

## Fatia 4 · Item do cardápio

### Item (ITE)

| ID | Cenário | Nível | Origem |
|---|---|---|---|
| ITE-01 | Item válido é criado com nome, descrição, preço, disponibilidade só no local e caminho da foto. | U | EN-IT |
| ITE-02 | Nome fora de 2 a 100 caracteres, ou descrição vazia ou acima de 500, é recusado. | U | EN-IT, Dec. W |
| ITE-03 | Espaços sobrando no nome são removidos: "  Feijoada   completa " vira "Feijoada completa". | U | Dec. S |
| ITE-04 | Preço zero ou negativo é recusado. | U | EN-IT, Dec. W |
| ITE-05 | Preço com mais de duas casas decimais é recusado, sem arredondar. | U | Dec. W |
| ITE-06 | Preço acima de 99.999.999,99 é recusado. | U | Dec. W |
| ITE-07 | Preço com uma casa decimal é guardado e devolvido com duas (39.9 vira 39.90). | U · I | Dec. W |
| ITE-08 | Caminho da foto ausente, acima de 255 caracteres ou sem extensão `.jpg`, `.jpeg`, `.png` ou `.webp` é recusado; caminho relativo e URL são aceitos. | U | EN-IT, Dec. W |
| ITE-09 | Disponibilidade só no local ausente devolve 400, sem assumir `false`. | I | EN-IT, Dec. W |
| ITE-10 | Cadastro em restaurante ativo é aceito. | C | EN-IT |
| ITE-11 | Cadastro em restaurante inexistente ou removido devolve não encontrado. | C | EN-IT |
| ITE-12 | Cadastro com o nome de outro item ativo do mesmo restaurante devolve conflito. | C · I · P | Dec. S |
| ITE-13 | O mesmo nome com maiúsculas, acentos ou espaço no fim diferentes também devolve conflito. | I | Dec. S, T |
| ITE-14 | O mesmo nome em outro restaurante é aceito. | C · I | Dec. S |
| ITE-15 | Atualizar um item sem mudar o nome é aceito: a verificação ignora o próprio item. | C · I | Dec. S |
| ITE-16 | Renomear para o nome de outro item ativo devolve conflito. | C | Dec. S |
| ITE-17 | Acessar, pela URL de um restaurante, um item que pertence a outro devolve 404. | C · I | Dec. M |
| ITE-18 | Excluir um item preenche `removido_em` e mantém a linha. | C · I | Dec. E |
| ITE-19 | Depois de excluir um item, criar outro com o mesmo nome é aceito. | I · P | Dec. S |
| ITE-20 | O banco recusa dois itens ativos com o mesmo nome no mesmo restaurante, mesmo se a verificação do caso de uso for contornada. | I | Dec. S |
| ITE-21 | CRUD HTTP aninhado: POST devolve 201 com `Location`; GET lista paginada com filtro `apenasNoLocal`; GET por id; PUT; DELETE devolve 204 e, depois dele, 404. | I · P | EN-IT, EN-1, Dec. M |
| ITE-22 | Todas as rotas de item de um restaurante removido devolvem 404. | I | Dec. E, M |

---

## Fatia 5 · Fechamento

A fatia 5 não acrescenta cenários. Ela confirma que todos os anteriores estão verdes no CI e percorre o checklist de entrega do briefing: README com guia de avaliação rápida, relatório em PDF organizado pelos nove critérios, matriz requisito → evidência gerada a partir deste catálogo, diagramas, EXPLAIN dos índices e o vídeo.
