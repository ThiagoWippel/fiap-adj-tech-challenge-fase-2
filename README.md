# API de Gestão de Usuários — Restaurantes

> **Fase 2 em desenvolvimento.** Este repositório continua o projeto da Fase 1,
> agora reescrito em Clean Architecture. A versão entregue e avaliada da Fase 1
> está na tag [`fase-1-entregue`](https://github.com/ThiagoWippel/fiap-adj-tech-challenge-fase-2/tree/fase-1-entregue), e é ela que o
> restante deste README descreve. Os cenários que a Fase 2 precisa comprovar
> estão em [`docs/catalogo-de-cenarios.md`](docs/catalogo-de-cenarios.md). A
> documentação da Fase 2 substitui este arquivo ao fim do desenvolvimento.

Backend do sistema compartilhado de gestão para restaurantes.
**Tech Challenge — Fase 1 · Pós Tech FIAP · Arquitetura e Desenvolvimento em Java**

Um grupo de restaurantes decidiu financiar em conjunto um sistema único de gestão,
em vez de cada estabelecimento manter o seu. Esta primeira fase entrega a base
sobre a qual as demais serão construídas: o cadastro de usuários e a validação de
credenciais.

---

## Índice

- [Escopo](#escopo)
- [Stack](#stack)
- [Como executar](#como-executar)
- [Endpoints](#endpoints)
- [Modelo de dados](#modelo-de-dados)
- [Tratamento de erros](#tratamento-de-erros)
- [Decisões de arquitetura](#decisões-de-arquitetura)
- [Verificação](#verificação)
- [Solução de problemas](#solução-de-problemas)
- [Melhorias futuras](#melhorias-futuras)

---

## Escopo

O sistema contempla dois tipos de usuário — **cliente** e **dono de restaurante** —
e oferece:

- Cadastro, consulta, atualização e exclusão de usuários
- Troca de senha em endpoint próprio, separado da atualização dos demais dados
- Busca de usuários pelo nome
- Registro automático da data da última alteração
- Unicidade de e-mail, login e documento
- Validação de credenciais (login e senha)

Restaurantes, cardápios, pedidos e avaliações são escopo das fases seguintes.

---

## Stack

| Componente | Versão | Papel |
|---|---|---|
| Java | 21 | Linguagem |
| Spring Boot | 4.0.7 | Framework de aplicação |
| Spring Data JPA / Hibernate | 7.2 | Persistência |
| MySQL | 8.4 | Banco relacional (produção) |
| H2 | 2.4 | Banco em memória (desenvolvimento) |
| springdoc-openapi | 3.1.0 | Documentação OpenAPI / Swagger |
| spring-security-crypto | — | Codificação de senhas com BCrypt |
| Docker Compose | v2 | Orquestração |

---

## Como executar

### Pré-requisitos

- Docker e Docker Compose
- Porta **8080** livre (aplicação) e **3307** livre (banco)

Não é necessário instalar Java, Maven ou MySQL: a compilação acontece dentro do
contêiner e o Maven Wrapper (`mvnw`) dispensa instalação prévia.

### Passo a passo

**1. Clonar o repositório**

```bash
git clone https://github.com/ThiagoWippel/fiap-adj-tech-challenge-fase-1.git
cd fiap-adj-tech-challenge-fase-1
```

**2. Criar o arquivo de variáveis de ambiente**

```bash
cp .env.example .env
```

Abra o `.env` e substitua as duas senhas por valores próprios. Evite os
caracteres `#`, `$`, `&`, aspas e espaços: no formato do arquivo, `#` inicia
comentário e `$` pode ser interpretado como variável, o que faria a senha chegar
truncada ao banco.

**3. Garantir a permissão de leitura do script de esquema**

```bash
chmod 644 docker/mysql/init/*.sql
```

O controle de versão preserva apenas o bit de execução, não as permissões de
leitura — o modo do arquivo após o clone depende do `umask` da máquina. Se o
arquivo ficar legível somente pelo proprietário, o contêiner do MySQL não
consegue lê-lo. Ver [Solução de problemas](#solução-de-problemas).

**4. Subir a aplicação**

```bash
docker compose up --build
```

A primeira execução baixa as imagens e compila o projeto; leva alguns minutos.
Nas seguintes, o cache de camadas reduz o tempo a poucos segundos.

**5. Confirmar**

| Recurso | Endereço |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Especificação OpenAPI | http://localhost:8080/v3/api-docs |
| Banco (ferramentas externas) | `localhost:3307` |

### Variáveis de ambiente

Todas são definidas no `.env` e consumidas pelo `docker-compose.yml`.

| Variável | Padrão no exemplo | Descrição |
|---|---|---|
| `DB_NAME` | `restaurante_db` | Nome do banco criado na primeira inicialização |
| `DB_USER` | `restaurante` | Usuário da aplicação |
| `DB_PASSWORD` | — | Senha do usuário da aplicação |
| `DB_ROOT_PASSWORD` | — | Senha do usuário root do MySQL |
| `DB_EXTERNAL_PORT` | `3307` | Porta exposta na máquina para acesso ao banco |
| `APP_PORT` | `8080` | Porta exposta na máquina para a aplicação |

O arquivo `.env` **não é versionado**. O `.env.example` serve de modelo.

### Encerrar

```bash
docker compose down      # para e remove os contêineres, preservando os dados
docker compose down -v   # remove também o volume — apaga o banco
```

### Execução local sem Docker

Para desenvolvimento, o perfil `dev` usa H2 em memória e dispensa contêineres:

```bash
./mvnw spring-boot:run
```

O console do H2 fica em http://localhost:8080/h2-console — informe
`jdbc:h2:mem:restaurante` no campo *JDBC URL*, usuário `sa` e senha em branco.
Os dados são recriados a cada inicialização.

> Os dois modos usam a porta 8080 e **não podem executar simultaneamente**.

---

## Endpoints

Base: `http://localhost:8080`

| Verbo | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/api/v1/usuarios` | Cadastra um usuário | 201 |
| GET | `/api/v1/usuarios/{id}` | Consulta por identificador | 200 |
| GET | `/api/v1/usuarios?nome=` | Busca por nome (lista) | 200 |
| GET | `/api/v2/usuarios?nome=` | Busca por nome (paginada) | 200 |
| PUT | `/api/v1/usuarios/{id}` | Atualiza os dados | 200 |
| PUT | `/api/v1/usuarios/{id}/senha` | Troca a senha | 204 |
| DELETE | `/api/v1/usuarios/{id}` | Exclui o usuário | 204 |
| POST | `/api/v1/auth/login` | Valida credenciais | 200 |

### Cadastro

```bash
curl -i -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Silva",
    "email": "maria.silva@exemplo.com",
    "login": "maria.silva",
    "senha": "SenhaSegura123",
    "tipo": "CLIENTE",
    "cpf": "123.456.789-09",
    "endereco": {
      "rua": "Rua das Flores", "numero": "123", "complemento": "Apto 45",
      "bairro": "Centro", "cidade": "Camboriú", "estado": "SC", "cep": "88340-000"
    }
  }'
```

Resposta `201 Created`, com o cabeçalho `Location` apontando para o recurso:

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria.silva@exemplo.com",
  "login": "maria.silva",
  "tipo": "CLIENTE",
  "documento": "12345678909",
  "endereco": {
    "rua": "Rua das Flores",
    "numero": "123",
    "complemento": "Apto 45",
    "bairro": "Centro",
    "cidade": "Camboriú",
    "estado": "SC",
    "cep": "88340000"
  },
  "dataCriacao": "2026-08-21T10:30:00",
  "dataUltimaAlteracao": "2026-08-21T10:30:00"
}
```

CPF, CNPJ e CEP são normalizados para conter apenas dígitos; a sigla do estado é
convertida para maiúsculas. A senha não integra nenhuma resposta da API.

Para cadastrar um dono de restaurante, informe `"tipo": "DONO_RESTAURANTE"` e
`cnpj` no lugar de `cpf`.

### Regras de validação

| Campo | Regra |
|---|---|
| `nome` | Obrigatório, 3 a 120 caracteres |
| `email` | Obrigatório, formato válido, até 255 caracteres, único |
| `login` | Obrigatório, 4 a 50 caracteres, único. Letras, números, ponto, hífen e sublinhado |
| `senha` | Obrigatória, 8 a 72 caracteres |
| `tipo` | Obrigatório: `CLIENTE` ou `DONO_RESTAURANTE` |
| `cpf` | Obrigatório para `CLIENTE`, com dígitos verificadores válidos, único |
| `cnpj` | Obrigatório para `DONO_RESTAURANTE`, com dígitos verificadores válidos, único |
| `endereco` | Obrigatório. Complemento é o único campo opcional |
| `cep` | Obrigatório, oito dígitos, com ou sem pontuação |
| `estado` | Obrigatório, exatamente duas letras |

O limite superior da senha não é arbitrário: o BCrypt processa no máximo 72 bytes
e **descarta silenciosamente** o excedente. Sem essa validação, uma senha mais
longa permitiria autenticação usando apenas o seu início.

### Busca por nome

```bash
curl "http://localhost:8080/api/v1/usuarios?nome=maria"
```

Retorna os usuários cujo nome contém o termo, sem diferenciar maiúsculas de
minúsculas. Omitindo o parâmetro, retorna todos. Quando nada corresponde, a
resposta é `200` com lista vazia — a coleção filtrada existe como recurso, apenas
não contém elementos.

### Atualização

```bash
curl -X PUT http://localhost:8080/api/v1/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Silva Souza",
    "email": "maria.souza@exemplo.com",
    "login": "maria.souza",
    "endereco": { "rua": "Rua Nova", "numero": "456", "bairro": "Centro",
      "cidade": "Itajaí", "estado": "SC", "cep": "88300000" }
  }'
```

Não aceita senha — a troca possui endpoint próprio. Não aceita tipo nem
documento: ambos são imutáveis após o cadastro.

### Troca de senha

```bash
curl -X PUT http://localhost:8080/api/v1/usuarios/1/senha \
  -H "Content-Type: application/json" \
  -d '{ "senhaAtual": "SenhaSegura123", "novaSenha": "SenhaNova456" }'
```

> **A senha atual é obrigatória.** O enunciado não menciona essa exigência; trata-se
> de decisão de segurança do projeto, adotada como proteção contra alteração
> indevida em sessão deixada aberta.

Retorna `204 No Content`. A data da última alteração é atualizada — trocar a senha
é, também, uma alteração do cadastro.

### Validação de credenciais

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "login": "maria.silva", "senha": "SenhaSegura123" }'
```

```json
{ "id": 1, "nome": "Maria Silva", "tipo": "CLIENTE" }
```

Retorna apenas o necessário para identificar quem autenticou. Não é uma consulta
de perfil: a operação responde se as credenciais conferem, não quem é o usuário.

### Versionamento

A versão integra o caminho da rota (`/api/v1/...`). Entre as alternativas
avaliadas — cabeçalho customizado e negociação de conteúdo pelo `Accept` — esta é
a única visível na documentação Swagger e na coleção Postman.

Mudanças compatíveis, como a inclusão de um campo em uma resposta, permanecem na
versão corrente: clientes existentes seguem funcionando e ignoram o campo novo.
Apenas alterações que quebram consumidores originam nova versão.

É o caso de `/api/v2/usuarios`: onde a v1 devolve um vetor de usuários, a v2
devolve um objeto contendo o vetor no campo `conteudo`, acompanhado dos metadados
de paginação. Um consumidor da v1 não consegue processar essa resposta — quebra de
contrato que justifica o incremento de versão.

```json
{
  "conteudo": [ /* ... */ ],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 42,
  "totalPaginas": 5,
  "ultima": false
}
```

Na existência de uma v3, a depreciação da v1 se daria por período de convivência
entre as versões, cabeçalhos de aviso nas respostas da versão obsoleta e, ao fim
do prazo, resposta indicando a retirada definitiva.

---

## Modelo de dados

Tabela única, decorrente da estratégia de herança adotada.

| Coluna | Tipo | Restrições |
|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `tipo_usuario` | VARCHAR(20) | NOT NULL — discriminadora |
| `nome` | VARCHAR(120) | NOT NULL |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE |
| `login` | VARCHAR(50) | NOT NULL, UNIQUE |
| `senha` | VARCHAR(100) | NOT NULL — hash BCrypt |
| `cpf` | VARCHAR(11) | NULL, UNIQUE |
| `cnpj` | VARCHAR(14) | NULL, UNIQUE |
| `data_criacao` | DATETIME(6) | NOT NULL |
| `data_ultima_alteracao` | DATETIME(6) | NOT NULL |
| `endereco_rua` | VARCHAR(150) | NOT NULL |
| `endereco_numero` | VARCHAR(10) | NOT NULL |
| `endereco_complemento` | VARCHAR(60) | NULL |
| `endereco_bairro` | VARCHAR(80) | NOT NULL |
| `endereco_cidade` | VARCHAR(80) | NOT NULL |
| `endereco_estado` | VARCHAR(2) | NOT NULL |
| `endereco_cep` | VARCHAR(9) | NOT NULL |

Script completo em [`docker/mysql/init/01-schema.sql`](docker/mysql/init/01-schema.sql),
executado pelo MySQL na primeira inicialização do contêiner.

**Dimensionamentos.** O e-mail acomoda 255 caracteres porque o limite normativo de
um endereço é 254. A senha reserva 100 embora o hash BCrypt tenha sempre 60 — a
folga evita acoplar o esquema a um algoritmo específico. O número do endereço é
texto porque existem valores como "S/N" e "123-A".

**Colunas de documento aceitam nulo** por consequência da estratégia de tabela
única: o registro de um cliente não preenche o CNPJ, e vice-versa. A
obrigatoriedade por tipo é garantida na camada de aplicação.

**Não há índice sobre `nome`**, deliberadamente. A busca é por conteúdo, e índices
de árvore só são aproveitados quando o termo aparece no início do valor. Criar o
índice daria aparência de otimização sem produzir nenhuma.

---

## Tratamento de erros

Todas as respostas de erro seguem a **RFC 7807** (*Problem Details for HTTP APIs*),
com o tipo de mídia `application/problem+json`.

Aos cinco campos padrão da especificação — `type`, `title`, `status`, `detail` e
`instance` — foram acrescentadas duas extensões: `momento`, que permite
correlacionar a resposta com o registro em log, e `erros`, presente nas falhas de
validação com o detalhamento por campo.

<a id="dados-invalidos"></a>

### Dados inválidos

`400 Bad Request` — um ou mais campos não passaram na validação.

```json
{
  "type": "https://github.com/ThiagoWippel/fiap-adj-tech-challenge-fase-1/blob/main/README.md#dados-invalidos",
  "title": "Dados invalidos",
  "status": 400,
  "detail": "Um ou mais campos da requisicao nao passaram na validacao",
  "instance": "/api/v1/usuarios",
  "momento": "2026-08-21T10:30:00",
  "erros": [
    { "campo": "email", "mensagem": "O e-mail informado nao e valido" },
    { "campo": "senha", "mensagem": "A senha deve ter entre 8 e 72 caracteres" }
  ]
}
```

<a id="regra-de-negocio"></a>

### Regra de negócio

`400 Bad Request` — a requisição é estruturalmente válida, mas viola uma regra que
depende da combinação de campos. Ocorre quando o documento informado não
corresponde ao tipo de usuário: CPF é exigido para `CLIENTE`, CNPJ para
`DONO_RESTAURANTE`.

<a id="credenciais-invalidas"></a>

### Credenciais inválidas

`401 Unauthorized` — as credenciais não conferem.

A resposta é **idêntica** para login inexistente e senha incorreta, tanto em
mensagem quanto em tempo de processamento. Diferenciar os dois casos permitiria
descobrir quais logins existem no sistema por tentativa e erro.

Também ocorre na troca de senha, quando a senha atual informada está incorreta.

<a id="recurso-nao-encontrado"></a>

### Recurso não encontrado

`404 Not Found` — não existe usuário com o identificador informado.

<a id="conflito-de-dados"></a>

### Conflito de dados

`409 Conflict` — a requisição é válida, mas conflita com o estado atual do banco:
e-mail, login, CPF ou CNPJ já cadastrados.

A verificação ocorre em duas camadas. O serviço consulta antes de gravar para
produzir mensagem legível; as restrições do banco garantem a integridade quando
duas requisições simultâneas passam por essa verificação e colidem na escrita.

<a id="erro-interno"></a>

### Erro interno

`500 Internal Server Error` — falha não prevista.

O rastro da exceção é registrado em log e **nunca** integra a resposta: mensagens
internas revelam estrutura de pacotes, versões de biblioteca e por vezes trechos de
consulta ao banco.

---

## Decisões de arquitetura

### Organização em camadas

```
config/                  configuração de Swagger, BCrypt e auditoria
controllers/             tradução entre HTTP e serviços
controllers/handlers/    tratamento centralizado de erros
dtos/request/            contratos de entrada, com validações
dtos/response/           contratos de saída
entities/                modelo de domínio
mappers/                 conversão entre entidades e DTOs
repositories/            acesso a dados
services/                regras de negócio
services/exceptions/     exceções de negócio
```

Controllers não contêm regra de negócio: traduzem HTTP para chamadas de serviço e
devolvem o código de status adequado.

### Herança em tabela única

`Usuario` é abstrata; `Cliente` e `DonoRestaurante` são as implementações
concretas, distinguidas por coluna discriminadora.

A estratégia `JOINED` exigiria junção em toda leitura e triplicaria o número de
tabelas. `TABLE_PER_CLASS` foi descartada por inviabilizar um requisito: a
unicidade de e-mail precisa valer no sistema inteiro, e uma restrição de unicidade
não abrange tabelas distintas.

O tipo do usuário é resolvido por **polimorfismo**, não por leitura da coluna
discriminadora — cada subclasse declara o próprio tipo e o próprio documento.
Adicionar um terceiro tipo de usuário significa criar uma classe nova, sem alterar
código existente.

### Endereço como objeto de valor

O endereço não possui identidade própria nem ciclo de vida independente: não existe
"um endereço" no sistema, existe "o endereço de um usuário". Por isso é um
`@Embeddable`, cujos campos se tornam colunas da própria tabela de usuário, sem
tabela adicional nem relacionamento.

### Senhas

Codificadas com **BCrypt**, através da biblioteca `spring-security-crypto` — a
camada de criptografia isolada, sem a cadeia de filtros do Spring Security
completo, que o enunciado dispensa.

O algoritmo é unidirecional: o valor armazenado não pode ser revertido, e a
verificação compara hashes. Incorpora um valor aleatório por registro, de modo que
senhas idênticas produzem hashes diferentes — impedindo que senhas repetidas sejam
identificadas por inspeção do banco.

### Criação do esquema

A criação das tabelas é responsabilidade do banco, não da aplicação. No perfil
`docker`, o Hibernate opera em modo `validate`: confere se a estrutura corresponde
ao mapeamento das entidades, sem criá-la nem alterá-la.

Assim a estrutura do banco é um artefato explícito e versionado, e não efeito
colateral do mapeamento objeto-relacional. Como consequência, qualquer divergência
entre o script e as entidades impede a aplicação de iniciar, apontando o campo
responsável.

### Exclusão física

A exclusão lógica foi avaliada e descartada. Manter o registro marcado como inativo
preservaria o e-mail ocupado na restrição de unicidade, impedindo que a mesma
pessoa se recadastrasse. Contornar isso exigiria limpar o e-mail na exclusão ou
adotar restrição condicional.

### Padrões aplicados

**Factory Method** — o campo `tipo` do cadastro determina qual subclasse
instanciar. A decisão fica isolada em `UsuarioFactory`; o serviço lida apenas com
`Usuario` e não conhece as subclasses.

**Injeção de dependência por construtor** — todas as dependências chegam em campos
finais. Não há como construir uma classe em estado incompleto, e os serviços podem
ser testados sem subir o contexto do Spring.

---

## Verificação

O script `verificar-api.sh` exercita todos os endpoints e confere tanto os códigos
de status quanto o formato das respostas.

```bash
chmod +x verificar-api.sh
./verificar-api.sh
```

São 29 verificações, incluindo a ausência do campo de senha nas respostas, a
presença dos campos do ProblemDetail nos erros e os metadados de paginação na v2.
O script remove ao final os registros que criou, podendo ser repetido sem deixar
resíduo.

Testes automatizados:

```bash
./mvnw test
```

---

## Solução de problemas

### `Permission denied` ao ler o script de esquema

```
/docker-entrypoint-initdb.d/01-schema.sql: Permission denied
```

O contêiner do MySQL aborta a inicialização e reinicia. Na segunda tentativa sobe
normalmente, **mas sem a tabela** — o passo de inicialização já foi marcado como
concluído. A aplicação então falha na validação do esquema e entra em laço de
reinício.

O arquivo precisa ser legível por todos; o processo dentro do contêiner executa sob
outro usuário.

```bash
chmod 644 docker/mysql/init/*.sql
docker compose down -v
docker compose up --build
```

### `address already in use` na porta 8080

Outra aplicação ocupa a porta — com frequência, uma execução local via
`./mvnw spring-boot:run`.

```bash
lsof -i :8080
```

Encerre o processo indicado, ou altere `APP_PORT` no `.env`.

### `UnknownHostException: mysql`

A aplicação não resolve o nome do serviço de banco. Ocorre quando uma subida
anterior falhou parcialmente e os contêineres ficaram em redes distintas.

```bash
docker compose down
docker compose up
```

O `down` remove contêineres e rede em conjunto; na subida seguinte ambos entram na
mesma rede recém-criada.

### Alterações no esquema não surtem efeito

O MySQL executa os scripts de `/docker-entrypoint-initdb.d/` apenas na **primeira**
inicialização, com o volume ainda vazio.

```bash
docker compose down -v
docker compose up --build
```

> `docker compose down -v` apaga os dados do banco.

---

## Melhorias futuras

- **Autenticação com JWT** via Spring Security e, em conjunto, migração para
  identificadores não sequenciais. O risco de enumeração de recursos foi avaliado:
  identificadores sequenciais permitem inferir o volume de registros, mas nesta
  fase os endpoints não possuem controle de acesso — sem autorização, ocultar o
  identificador não protege o recurso. A exposição decorre da ausência de
  autenticação, não do formato da chave.
- **Exclusão lógica**, com tratamento do conflito com a unicidade de e-mail.
- **Endereço como entidade própria**, permitindo múltiplos endereços por usuário.
- **Migrações versionadas** com Flyway, no lugar do script único de inicialização.
- **Ampliação da cobertura de testes** para as camadas de controller e repositório.
- **Separação de dependências por perfil de build**, mantendo H2 e o respectivo
  console fora do artefato de produção.

---

## Autor

**Thiago Wippel Chaves** — RM375015
Pós Tech FIAP · Arquitetura e Desenvolvimento em Java · Fase 1
