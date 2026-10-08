# Matriz requisito → evidência

Gerada a partir do [catálogo de cenários](catalogo-de-cenarios.md), dos testes (`@DisplayName` com o ID do
cenário), da [collection do Postman](../postman/tech-challenge-fase-2.postman_collection.json) e do
[workflow do CI](../.github/workflows/ci.yml). Cada requisito leva aos cenários que o comprovam, e cada
cenário leva ao teste que roda.

Níveis: **U** domínio · **C** caso de uso · **I** integração com MySQL · **P** collection (Newman no CI) ·
**A** regra de arquitetura · **CI** pipeline.

**175 de 175 cenários com evidência.**

## Requisito → cenários

| Requisito | Descrição | Cenários |
|---|---|---|
| EN-OBJ | Enunciado, seção "Objetivo" ("expande o sistema", "código limpo", "execução integrada") | INF-03, ACE-01 |
| EN-TU | Enunciado, seção "Tipo de usuário" | TIP-01, TIP-03, TIP-05, TIP-06, TIP-08, TIP-10, TIP-11, TIP-12, TIP-14, TIP-15, TIP-16, TIP-17, TIP-18, TRO-01, TRO-05, TRO-08, TRO-09 |
| EN-RE | Enunciado, seção "Cadastro de restaurante" | RES-01, RES-02, RES-03, RES-05, RES-06, RES-08, RES-10, RES-12, RES-13, RES-14, RES-15, RES-16, RES-17, HOR-01, HOR-10, COZ-01, COZ-02, COZ-03 |
| EN-IT | Enunciado, seção "Cadastro dos itens do cardápio" | ITE-01, ITE-02, ITE-04, ITE-08, ITE-09, ITE-10, ITE-11, ITE-21 |
| EN-1 | Critério 1 · Funcionalidade: os três cadastros e os endpoints funcionando | TIP-16, TRO-08, RES-12, ITE-21 |
| EN-2 | Critério 2 · Qualidade do código: práticas do Spring Boot, código organizado e documentado | Javadoc nas classes públicas, Swagger (INF-05) e o README; ver o relatório |
| EN-3 | Critério 3 · Documentação: arquitetura, endpoints, configuração e execução | INF-05 |
| EN-4 | Critério 4 · Collections para teste | ACE-02 |
| EN-5 | Critério 5 · Docker Compose com a aplicação e o banco | INF-02, INF-03, ACE-02 |
| EN-6 | Critério 6 · Repositório aberto | INF-07 |
| EN-7 | Critério 7 · Clean Architecture: camadas e separação de responsabilidades | ARQ-01, ARQ-02, ARQ-03, ARQ-04, RES-18 |
| EN-8 | Critério 8 · Testes unitários com 80% de cobertura e testes de integração | INF-01, INF-04, INF-07 |
| EN-9 | Critério 9 · Vídeo | Vídeo de apresentação, entregue com o relatório |
| FB-1 | Feedback da Fase 1: login sem testes e geração de tokens | USU-06, LOG-01, LOG-02, LOG-04 |
| FB-2 | Feedback da Fase 1: busca por nome na collection | USU-27, USU-28 |
| FB-3 | Feedback da Fase 1: respostas de erro em ProblemDetail | ERR-01, ERR-02, ERR-10 |
| F1-01 | Cadastro, atualização e exclusão de usuários | USU-06, USU-08, USU-14, USU-18, USU-24, USU-29, EXC-01, EXC-02 |
| F1-02 | Troca de senha em endpoint separado | USU-15, USU-16, USU-26, LOG-05 |
| F1-03 | Atualização dos demais dados em endpoint distinto do de senha | USU-11, USU-25 |
| F1-04 | Registro da data da última alteração | USU-25, USU-30 |
| F1-05 | Busca de usuários pelo nome | USU-17, USU-27, USU-28 |
| F1-06 | Unicidade do e-mail | USU-07, USU-12, USU-13, USU-20, USU-21 |
| F1-07 | Serviço de validação de login | INF-09, LOG-01, LOG-02, LOG-03, LOG-04, LOG-05, LOG-07 |
| F1-08 | Os dois tipos de usuário obrigatórios | USU-19 |
| F1-09 | Campos obrigatórios: nome, e-mail, login, senha, data da última alteração e endereço | END-01, END-02, END-03, END-04, END-05, USU-01, USU-02, USU-03, USU-04, USU-22 |
| F1-10 | Estratégia de versionamento de API | USU-27, USU-28 |
| F1-11 | Respostas de erro no padrão ProblemDetail | ERR-01, ERR-03, ERR-04, ERR-05, ERR-06, ERR-07 |
| F1-12 | Swagger com exemplos de sucesso e de erro | INF-05 |

## Fatia 0 · Fundação

| ID | Cenário | Nível | Testes | Collection e CI |
|---|---|---|---|---|
| ARQ-01 | Os pacotes `domain` e `application` não dependem de framework: Spring, Jakarta, Hibernate nem Jackson. | A | `ArquiteturaTest.nucleoNaoDependeDeFramework` | — |
| ARQ-02 | Os pacotes `domain`, `application` e `interfaceadapter` não dependem de `infrastructure`. | A | `ArquiteturaTest.nucleoNaoDependeDaInfraestrutura` | — |
| ARQ-03 | As camadas respeitam a direção das dependências: infraestrutura → adaptadores → aplicação → domínio. | A | `ArquiteturaTest.camadasRespeitamADirecaoDasDependencias` | — |
| ARQ-04 | O pacote `interfaceadapter` não usa classes nem anotações do Spring: controllers, gateways e presenters são Java puro. | A | `ArquiteturaTest.adaptadoresNaoDependemDeFramework` | — |
| INF-01 | Dado o MySQL do Testcontainers criado pelo script de schema, quando o contexto sobe, então o Hibernate em modo `validate` aceita o mapeamento de todas as entidades. | I | `InicializacaoIT.deveSubirContraOMysqlDoConteiner_ComHibernateEmModoValidate`<br>`InicializacaoIT.deveCriarAsTabelasPeloScript` | — |
| INF-02 | Quando consulto `/actuator/health` com o banco no ar, então recebo `UP`. | I | `SaudeIT.deveResponderUp_QuandoOBancoEstiverNoAr` | — |
| INF-03 | Dado um clone limpo e o `.env` copiado do `.env.example`, quando rodo `docker compose up`, então aplicação e banco sobem e o healthcheck da aplicação passa. | CI | — | CI: Subir a aplicação e o banco a partir do .env.example (INF-03) |
| INF-04 | Quando a cobertura dos testes unitários no núcleo (`domain`, `application`, `interfaceadapter`) ou a cobertura total cai abaixo de 80% de linha ou de ramo, então o build falha. Testado de propósito uma vez. | CI | — | CI: Os dois cortes de cobertura travam o build abaixo de 80% (INF-04) |
| INF-05 | Quando consulto `/v3/api-docs`, então os 26 endpoints aparecem, cada um com ao menos um exemplo de sucesso e um de erro. | I | `DocumentacaoIT.deveServirAEspecificacaoOpenApi`<br>`DocumentacaoIT.deveDocumentarExemplosDeSucessoEDeErro` | — |
| INF-06 | Dado o perfil `docker` sem a chave de assinatura do token, quando a aplicação sobe, então ela falha na inicialização com uma mensagem que nomeia a variável de ambiente que falta. | I | `ConfiguracaoDoTokenIT.deveFalharSemAChave`<br>`ConfiguracaoDoTokenIT.deveFalharComChaveCurta`<br>`ConfiguracaoDoTokenIT.deveCarregarAsPropriedades` | — |
| INF-07 | A cada push, o GitHub Actions roda `mvn verify` (unitários, integração com Testcontainers, ArchUnit e os cortes de cobertura) e termina verde. | CI | — | CI: Testes, arquitetura e cobertura (INF-07) |
| INF-08 | A porta de transação desfaz tudo o que foi gravado quando a operação lança exceção, e confirma as gravações quando ela termina sem erro. | U · I | `ITransactionManagerTest.deveExecutarOperacaoSemRetornoDentroDaTransacao`<br>`GerenciadorDeTransacaoIT.deveDesfazerAsGravacoes_QuandoAOperacaoFalhar`<br>`GerenciadorDeTransacaoIT.deveConfirmarAsGravacoes_QuandoAOperacaoTerminarSemErro` | — |
| INF-09 | A porta de senha gera um hash BCrypt diferente a cada codificação, nunca o texto original, e confere a senha correta. | U | `CodificadorDeSenhaBCryptTest.deveGerarHashBCrypt`<br>`CodificadorDeSenhaBCryptTest.deveGerarHashesDiferentesParaAMesmaSenha`<br>`CodificadorDeSenhaBCryptTest.deveConferirASenhaCorretaERecusarAErrada` | — |
| ERR-01 | Quando qualquer erro acontece, então a resposta é `application/problem+json` com `type`, `title`, `status`, `detail`, `instance` e `momento`. | I | `ExcecoesDaAplicacaoTest.recursoNaoEncontradoDeveCarregarAMensagem`<br>`ExcecoesDaAplicacaoTest.conflitoDeDadosDeveCarregarAMensagem`<br>`ExcecoesDaAplicacaoTest.credenciaisInvalidasDeveCarregarAMensagem`<br>`ExcecoesDeDominioTest.regraDeNegocioDeveCarregarAMensagem`<br>`TratadorDeErrosIT.deveResponderNoFormatoProblemDetail`<br>`TratadorDeErrosIT.naoDeveExporAMensagemDoBanco_QuandoHouverViolacaoDeIntegridade`<br>`TratadorDeErrosTest.deveFormatarOMomentoSempreComSegundos` | Postman: 2. E-mail duplicado<br>Postman: 7. Consulta de usuario inexistente |
| ERR-02 | O `type` aponta para o namespace estável da própria aplicação (`/problemas/{tipo}`), e essa URL responde 200 com a descrição do problema. | I | `TratadorDeErrosIT.deveDescreverOTipoDeProblemaNaUrlDoType`<br>`TratadorDeErrosIT.deveListarTodosOsTiposDeProblema`<br>`TratadorDeErrosIT.deveDevolver404_QuandoOTipoDeProblemaNaoExistir`<br>`TipoDeProblemaTest.deveMontarOEnderecoAbsolutoDoTipo`<br>`TipoDeProblemaTest.deveEncontrarOTipoPeloIdentificador` | — |
| ERR-03 | Dado um corpo que falha no Bean Validation, então a resposta traz a extensão `erros` com o campo e a mensagem de cada falha. | I | `TratadorDeErrosIT.deveListarOsCamposInvalidos_QuandoAValidacaoFalhar` | — |
| ERR-04 | Dado um JSON malformado, então a resposta é 400 em ProblemDetail; dado um corpo que não é JSON, a resposta é 415. | I | `TratadorDeErrosIT.deveDevolver400_QuandoOJsonEstiverMalformado`<br>`TratadorDeErrosIT.deveDevolver415_QuandoOCorpoNaoForJson` | — |
| ERR-05 | Dado um verbo HTTP não suportado na rota, então a resposta é 405 em ProblemDetail. | I | `TratadorDeErrosIT.deveDevolver405_QuandoOVerboNaoForSuportado` | — |
| ERR-06 | Dada uma rota inexistente, então a resposta é 404 em ProblemDetail. | I | `TratadorDeErrosIT.deveDevolver404_QuandoARotaNaoExistir` | — |
| ERR-07 | Dada uma falha inesperada, então a resposta é 500 sem rastro de pilha no corpo; o rastro vai só para o log. | I | `TratadorDeErrosIT.deveEsconderODetalheInterno_QuandoHouverFalhaInesperada` | — |
| ERR-08 | `ValidacaoDeDominioException` é uma `IllegalArgumentException` e vira 400; uma `IllegalArgumentException` lançada fora do domínio vira 500. | U · I | `ExcecoesDeDominioTest.validacaoDeDominioDeveSerIllegalArgumentException`<br>`TratadorDeErrosIT.deveDevolver500_QuandoUmaIllegalArgumentExceptionVierDeForaDoDominio` | — |
| ERR-09 | Títulos e mensagens saem em português com acento, por exemplo "Regra de negócio violada". | I | `TratadorDeErrosIT.deveTraduzirOsErrosDoFramework` | — |
| ERR-10 | A extensão `momento` é preenchida num único lugar e aparece em todos os erros, inclusive nos gerados pelo próprio Spring (400, 404 e 405). | I | `TratadorDeErrosIT.deveIncluirOMomento_NosErrosDoFramework`<br>`TratadorDeErrosTest.naoDeveAlterarCorpoQueNaoSejaProblemDetail` | — |
| PAG-01 | Dada uma listagem sem parâmetros, então vêm 10 itens por página, ordenados por nome, no formato `conteudo` mais os metadados de paginação. | I | `PaginaTest.deveIndicarSeEAUltimaPagina`<br>`PaginaTest.deveConverterOConteudo`<br>`PaginacaoDasListagensIT.deveUsarOPadrao`<br>`PaginacaoTest.deveConverterOPageable`<br>`UsuarioV2ApiIT.deveUsarOPadraoDeDezPorPaginaOrdenadoPorNome`<br>`UsuarioV2ApiIT.devePaginarEOrdenarDecrescente` | Postman: 9. Listar tipos |
| PAG-02 | Dado `size` acima de 50, então a página vem com no máximo 50 itens e os metadados mostram o tamanho efetivo. | I | `PaginacaoDasListagensIT.deveLimitarOTamanho`<br>`UsuarioV2ApiIT.deveLimitarOTamanhoDaPagina` | — |
| PAG-03 | Dado `sort` por um campo que não existe, então a resposta é 400. | I | `PaginacaoDasListagensIT.deveRecusarOrdenacaoDesconhecida`<br>`PaginacaoTest.deveRecusarCampoDeOrdenacaoDesconhecido`<br>`UsuarioV2ApiIT.deveRecusarOrdenacaoDesconhecida` | — |
| PAG-04 | Dada uma página além da última, então a resposta é 200 com conteúdo vazio. | I | `PaginacaoDasListagensIT.deveDevolverPaginaVazia`<br>`UsuarioV2ApiIT.deveDevolverPaginaVaziaAlemDaUltima` | — |
| PAG-05 | Registros removidos nunca aparecem em nenhuma listagem. | I | `UsuarioV2ApiIT.deveIgnorarUsuariosRemovidos` | — |
| PAG-06 | A contagem de elementos nos metadados considera só registros ativos. | I | `UsuarioV2ApiIT.deveIgnorarUsuariosRemovidos` | — |

## Fatia 1 · Usuário

| ID | Cenário | Nível | Testes | Collection e CI |
|---|---|---|---|---|
| END-01 | Endereço com rua, número, bairro, cidade, UF e CEP é aceito; o complemento é opcional. | U | `EnderecoTest.deveAceitarEnderecoCompleto_ComComplementoOpcional` | — |
| END-02 | O número aceita valores como "S/N" e "123-A". | U | `EnderecoTest.deveAceitarNumeroComoTexto` | Postman: 1. Cadastrar dono de restaurante (CNPJ) |
| END-03 | UF fora das 27 siglas brasileiras é recusada. | U | `EnderecoTest.deveValidarAUf` | — |
| END-04 | CEP sem 8 dígitos é recusado; CEP com hífen é aceito e guardado só com dígitos. | U | `EnderecoTest.deveGuardarOCepSoComDigitos`<br>`EnderecoTest.deveRecusarCepInvalido` | Postman: 1. Cadastrar cliente (CPF) |
| END-05 | Rua, número, bairro, cidade, UF ou CEP ausente é recusado. | U | `EnderecoTest.deveRecusarRuaAusente`<br>`EnderecoTest.deveRecusarDemaisCamposAusentes`<br>`EnderecoTest.deveRecusarCamposLongosDemais` | — |
| DOC-01 | CPF com dígitos verificadores válidos é aceito; com máscara, é aceito e guardado só com dígitos. | U | `DocumentoTest.deveAceitarCpfValido`<br>`DocumentoTest.deveIdentificarOTipoPelaQuantidadeDeDigitos` | Postman: 1. Cadastrar cliente (CPF) |
| DOC-02 | CPF com dígitos verificadores inválidos, ou com todos os dígitos iguais, é recusado. | U | `DocumentoTest.deveRecusarCpfInvalido`<br>`DocumentoTest.deveRecusarDocumentoAusente` | Postman: 2. CPF com digitos verificadores invalidos |
| DOC-03 | CNPJ válido é aceito; com máscara, é aceito e guardado só com dígitos. | U | `DocumentoTest.deveAceitarCnpjValido` | Postman: 1. Cadastrar dono de restaurante (CNPJ) |
| DOC-04 | CNPJ com dígitos verificadores inválidos, ou com todos os dígitos iguais, é recusado. | U | `DocumentoTest.deveRecusarCnpjInvalido` | — |
| DOC-05 | Usuário do tipo Dono de Restaurante exige CNPJ; com CPF, é recusado. | U | `CadastrarUsuarioUseCaseTest.deveUsarOCnpjParaDonoDeRestaurante`<br>`CadastrarUsuarioUseCaseTest.deveRecusarDonoSemCnpj`<br>`UsuarioTest.donoDeRestauranteDeveExigirCnpj` | — |
| DOC-06 | Usuário de qualquer outro tipo exige CPF; com CNPJ, é recusado. | U | `UsuarioTest.demaisTiposDevemExigirCpf` | Postman: 2. CPF ausente para tipo CLIENTE<br>Postman: 9. Cadastrar usuário do tipo novo |
| USU-01 | Usuário válido é criado com nome, e-mail, login, senha já codificada, endereço, tipo e documento. | U | `UsuarioTest.deveCriarUsuarioValido`<br>`UsuarioTest.deveReconstituirUsuarioComIdEDatas`<br>`UsuarioTest.deveExigirSenhaEEndereco`<br>`UsuarioTest.deveCompararUsuariosPeloId` | — |
| USU-02 | Nome vazio ou fora de 3 a 120 caracteres é recusado. | U | `UsuarioTest.deveRecusarNomeInvalido`<br>`UsuarioTest.deveRecusarNomeLongoDemais` | — |
| USU-03 | E-mail em formato inválido é recusado. | U | `UsuarioTest.deveRecusarEmailInvalido`<br>`UsuarioTest.deveRecusarEmailLongoDemais` | — |
| USU-04 | Login fora de 4 a 50 caracteres, ou com caractere que não seja letra, número, ponto, hífen ou sublinhado, é recusado. | U | `UsuarioTest.deveRecusarLoginInvalido` | — |
| USU-05 | Alterar o nome para um valor inválido falha, e o usuário continua com o nome anterior. | U | `AtualizarUsuarioUseCaseTest.deveRecusarEnderecoAusente`<br>`UsuarioTest.deveManterOValorAnterior_QuandoAAlteracaoFalhar` | — |
| USU-06 | No cadastro válido, a senha passa pela porta de codificação e nunca é gravada em texto. | C | `CadastrarUsuarioUseCaseTest.deveCadastrarComASenhaCodificada`<br>`CadastrarUsuarioUseCaseTest.deveCadastrarDentroDeUmaTransacao`<br>`UsuarioGatewayTest.deveConverterNosDoisSentidosAoIncluir` | — |
| USU-07 | Cadastro com e-mail já usado devolve conflito e não grava nada. | C | `CadastrarUsuarioUseCaseTest.deveRecusarEmailJaCadastrado`<br>`UsuarioGatewayTest.deveRepassarVerificacoesEAnonimizacao` | — |
| USU-08 | Cadastro com login já usado devolve conflito. | C | `CadastrarUsuarioUseCaseTest.deveRecusarLoginJaCadastrado` | — |
| USU-09 | Cadastro com documento já usado devolve conflito. | C | `CadastrarUsuarioUseCaseTest.deveRecusarDocumentoJaCadastrado` | — |
| USU-10 | Cadastro com código de tipo inexistente devolve não encontrado. | C | `CadastrarUsuarioUseCaseTest.deveRecusarTipoInexistente`<br>`UsuarioApiIT.deveRecusarTipoInexistente` | — |
| USU-11 | A atualização de dados altera nome, e-mail, login e endereço, sem tocar na senha nem no tipo. | C | `AtualizarUsuarioUseCaseTest.deveAtualizarOsDados`<br>`UsuarioTest.deveAtualizarOsDadosPelosSetters`<br>`UsuarioControllerTest.deveAtualizar`<br>`UsuarioGatewayTest.deveConverterNosDoisSentidosAoAtualizar` | — |
| USU-12 | Atualização com o e-mail de outro usuário devolve conflito. | C | `AtualizarUsuarioUseCaseTest.deveRecusarEmailDeOutroUsuario`<br>`AtualizarUsuarioUseCaseTest.deveRecusarLoginDeOutroUsuario`<br>`UsuarioApiIT.deveRecusarAtualizacaoInvalida` | Postman: 4. E-mail pertencente a outro usuario |
| USU-13 | Atualização mantendo o próprio e-mail é aceita. | C | `AtualizarUsuarioUseCaseTest.deveAceitarOsPropriosEmailELogin`<br>`JpaUsuarioDataSourceIT.deveIgnorarOProprioUsuarioNasVerificacoes` | — |
| USU-14 | Atualização de usuário inexistente ou removido devolve não encontrado. | C | `AtualizarUsuarioUseCaseTest.deveRecusarUsuarioInexistente`<br>`UsuarioApiIT.deveRecusarAtualizacaoInvalida` | Postman: 4. Usuario inexistente |
| USU-15 | Troca de senha com a senha atual correta grava a nova senha codificada. | C | `TrocarSenhaUseCaseTest.deveGravarANovaSenhaCodificada`<br>`UsuarioControllerTest.deveTrocarASenha` | — |
| USU-16 | Troca de senha com a senha atual incorreta devolve credenciais inválidas e não grava nada. | C | `TrocarSenhaUseCaseTest.deveRecusarSenhaAtualIncorreta` | — |
| USU-17 | A busca por nome tira os espaços das pontas do termo; termo vazio devolve todos os usuários ativos. | C | `BuscarUsuariosPorNomeUseCaseTest.deveAparOTermo`<br>`BuscarUsuariosPorNomeUseCaseTest.deveBuscarTodos_QuandoNaoHouverTermo` | — |
| USU-18 | POST `/api/v1/usuarios` com cliente e CPF devolve 201, cabeçalho `Location`, tipo `CLIENTE` e o documento, sem o campo senha. | I · P | `UsuarioApiIT.deveCadastrarCliente`<br>`UsuarioControllerTest.deveCadastrar`<br>`UsuarioPresenterTest.deveMontarARespostaDoUsuario`<br>`UsuarioPresenterTest.deveAceitarUsuarioSemDatas` | Postman: 1. Cadastrar cliente (CPF)<br>Postman: 9. Cadastrar usuário do tipo novo<br>Postman: 12. Cadastrar um cliente |
| USU-19 | POST com dono e CNPJ devolve 201 e tipo `DONO_RESTAURANTE`. | I · P | `UsuarioApiIT.deveCadastrarDonoDeRestaurante` | Postman: 1. Cadastrar dono de restaurante (CNPJ)<br>Postman: 12. Cadastrar a dona do restaurante<br>Postman: 14. Cadastrar a dona dos restaurantes |
| USU-20 | POST com e-mail duplicado devolve 409. | I · P | `UsuarioApiIT.deveRecusarEmailDuplicado` | Postman: 2. E-mail duplicado |
| USU-21 | POST com `Maria@Exemplo.com` quando `maria@exemplo.com` já existe devolve 409. | I | `UsuarioApiIT.deveRecusarEmailQueSoMudaNasMaiusculas`<br>`JpaUsuarioDataSourceIT.deveBarrarEmailRepetidoNoBanco` | — |
| USU-22 | POST com campos obrigatórios ausentes devolve 400, com `erros` nomeando cada campo. | I · P | `CadastrarUsuarioUseCaseTest.deveRecusarEnderecoAusente`<br>`UsuarioApiIT.deveApontarOsCamposInvalidos`<br>`UsuarioApiIT.deveApontarOCpfInvalido` | Postman: 2. Campos obrigatorios ausentes ou invalidos<br>Postman: 2. CPF com digitos verificadores invalidos |
| USU-23 | POST de cliente sem CPF devolve 400 com o título "Regra de negócio violada". | I · P | `CadastrarUsuarioUseCaseTest.deveRecusarClienteSemCpf`<br>`UsuarioApiIT.deveRecusarClienteSemCpf` | Postman: 2. CPF ausente para tipo CLIENTE |
| USU-24 | GET `/api/v1/usuarios/{id}` devolve 200; id inexistente devolve 404 com `instance` igual à rota chamada. | I · P | `BuscarUsuarioPorIdUseCaseTest.deveDevolverOUsuarioEncontrado`<br>`BuscarUsuarioPorIdUseCaseTest.deveRecusarUsuarioInexistente`<br>`UsuarioApiIT.deveBuscarPorId`<br>`UsuarioControllerTest.deveBuscarPorId`<br>`UsuarioGatewayTest.deveBuscarPorIdEPorLogin` | Postman: 7. Consulta de usuario existente<br>Postman: 7. Consulta de usuario inexistente |
| USU-25 | PUT `/api/v1/usuarios/{id}` devolve 200; `dataUltimaAlteracao` avança e `dataCriacao` não muda. | I · P | `UsuarioApiIT.deveAtualizarMantendoADataDeCriacao`<br>`UsuarioPresenterTest.deveCortarAsFracoesDeSegundo` | Postman: 4. Atualizacao com sucesso |
| USU-26 | PUT `/api/v1/usuarios/{id}/senha` devolve 204; senha atual errada devolve 401; nova senha fora de 8 a 72 caracteres devolve 400. | I · P | `CadastrarUsuarioUseCaseTest.deveRecusarSenhaForaDoLimite`<br>`TrocarSenhaUseCaseTest.deveRecusarNovaSenhaForaDoLimite`<br>`TrocarSenhaUseCaseTest.deveRecusarUsuarioInexistente`<br>`SenhaEmTextoTest.deveAceitarSenhaDentroDoLimite`<br>`SenhaEmTextoTest.deveRecusarSenhaCurtaOuAusente`<br>`SenhaEmTextoTest.deveRecusarSenhaLongaDemais`<br>`UsuarioApiIT.deveTrocarASenha` | Postman: 3. Senha atual incorreta<br>Postman: 3. Nova senha fora das regras<br>Postman: 3. Usuario inexistente<br>Postman: 3. Troca de senha com sucesso |
| USU-27 | GET `/api/v1/usuarios?nome=` devolve lista simples nos três casos: com resultados, sem resultados e sem filtro. | I · P | `UsuarioApiIT.deveBuscarPorNomeEmLista`<br>`UsuarioControllerTest.deveBuscarPorNomeEmListaEPaginado`<br>`UsuarioGatewayTest.deveBuscarPorNome`<br>`UsuarioPresenterTest.deveConverterListaEPagina` | Postman: 5. Busca com resultados<br>Postman: 5. Busca sem resultados<br>Postman: 5. Busca sem filtro |
| USU-28 | GET `/api/v2/usuarios?nome=` devolve página com `conteudo` e metadados. | I · P | `BuscarUsuariosPorNomeUseCaseTest.deveBuscarPaginado`<br>`BuscarUsuariosPorNomeUseCaseTest.deveBuscarTodosPaginado_QuandoNaoHouverTermo`<br>`UsuarioV2ApiIT.deveDevolverAPaginaFiltradaPeloNome` | Postman: 5. Busca paginada (v2) |
| USU-29 | Nenhuma resposta da API contém o campo senha. | I · P | `SenhaEmTextoTest.naoDeveExporASenhaNoToString`<br>`UsuarioApiIT.naoDeveExporASenha` | Postman: 1. Cadastrar cliente (CPF)<br>Postman: 4. Atualizacao com sucesso<br>Postman: 6. Credenciais validas |
| USU-30 | Persistir, atualizar e reler um usuário mantém `dataCriacao` e avança `dataUltimaAlteracao` (a armadilha do update). | I | `JpaUsuarioDataSourceIT.deveManterADataDeCriacaoNaAtualizacao` | — |
| LOG-01 | Login com credenciais válidas devolve id, nome, tipo, token e validade do token. | C · I · P | `AutenticarUsuarioUseCaseTest.deveAutenticarEDevolverOToken`<br>`AutenticacaoApiIT.deveAutenticar`<br>`AutenticacaoControllerTest.deveAutenticar`<br>`AutenticacaoPresenterTest.deveMontarARespostaDoLogin` | Postman: 6. Credenciais validas |
| LOG-02 | Login inexistente devolve 401 com a mesma mensagem da senha incorreta, sem revelar que o login não existe. | C · I · P | `AutenticarUsuarioUseCaseTest.deveRecusarLoginInexistente`<br>`AutenticacaoApiIT.deveResponderIgualParaLoginInexistenteESenhaErrada` | Postman: 6. Login inexistente |
| LOG-03 | Login inexistente ainda executa uma comparação de hash, para levar o mesmo tempo que uma senha errada. | C | `AutenticarUsuarioUseCaseTest.deveConferirASenhaMesmoComLoginInexistente`<br>`CodificadorDeSenhaBCryptTest.deveRecusarQuandoNaoHouverHash` | — |
| LOG-04 | Senha incorreta devolve 401. | C · I | `AutenticarUsuarioUseCaseTest.deveRecusarSenhaIncorreta`<br>`AutenticacaoApiIT.deveRecusarSenhaIncorreta` | — |
| LOG-05 | A senha antiga deixa de funcionar depois da troca. | I · P | `AutenticacaoApiIT.deveAceitarSoASenhaNovaDepoisDaTroca` | Postman: 6. Senha antiga apos a troca (deve falhar) |
| LOG-06 | Usuário removido não consegue fazer login. | C | `AutenticarUsuarioUseCaseTest.deveRecusarUsuarioRemovido` | — |
| LOG-07 | Login com corpo inválido (login ou senha ausente) devolve 400. | I | `AutenticarUsuarioUseCaseTest.deveRecusarCredenciaisAusentes`<br>`AutenticacaoApiIT.deveRecusarCorpoIncompleto` | Postman: 6. Campos obrigatorios ausentes |
| TOK-01 | O token emitido é um JWT assinado, com `sub` igual ao id do usuário, o código do tipo e a expiração definida na configuração. | U · I | `AutenticacaoApiIT.deveDevolverTokenAssinado`<br>`GeradorDeTokenJwtTest.deveGerarTokenAssinadoComOsDadosDoUsuario` | — |
| TOK-02 | Um token adulterado não passa na verificação da assinatura. | U | `GeradorDeTokenJwtTest.deveRecusarTokenAdulterado`<br>`GeradorDeTokenJwtTest.deveRecusarTokenDeOutraChave` | — |
| TOK-03 | Nenhum endpoint exige token nesta fase: requisição sem `Authorization` é atendida normalmente. | I | `UsuarioApiIT.naoDeveExigirToken` | — |
| EXC-01 | Excluir usuário sem restaurante ativo anonimiza o registro. | C | `ExcluirUsuarioUseCaseTest.deveAnonimizarOUsuario`<br>`UsuarioControllerTest.deveExcluir` | Postman: 8. Excluir o dono de restaurante<br>Postman: 8. Excluir o cliente<br>Postman: 11. Excluir o usuário<br>Postman: 13. Excluir o cliente |
| EXC-02 | Excluir usuário inexistente ou já removido devolve 404. | C · I · P | `ExcluirUsuarioUseCaseTest.deveRecusarUsuarioInexistente`<br>`UsuarioApiIT.deveExcluirUmaVezSo` | Postman: 8. Excluir novamente (deve falhar) |
| EXC-03 | Depois da exclusão, o GET devolve 404, a busca por nome não traz o usuário e o login falha. | I · P | `UsuarioApiIT.deveSumirDepoisDeExcluido` | — |
| EXC-04 | Depois da exclusão, a linha continua com id e datas; o nome vira "Usuário removido"; e-mail, login, senha, documento, endereço e tipo ficam nulos; `removido_em` é preenchido. | I | `JpaUsuarioDataSourceIT.deveAnonimizarOUsuario` | — |
| EXC-05 | Depois da exclusão, um novo cadastro com o mesmo e-mail, login e documento é aceito, com id novo. | I · P | `UsuarioApiIT.devePermitirRecadastroDepoisDaExclusao` | — |
| EXC-06 | O banco recusa um usuário ativo (sem `removido_em`) com e-mail, login, senha, documento, endereço ou tipo nulo. | I | `JpaUsuarioDataSourceIT.deveRecusarUsuarioAtivoIncompleto` | — |
| ACE-01 | A collection da Fase 1 roda contra a Fase 2 e passa inteira, com três ajustes deliberados e documentados: o `type` dos erros, os títulos com acento e a verificação de mensagem do login atualizada para as palavras acentuadas. | P · CI | — | CI: Rodar a collection com o Newman (ACE-01) |
| ACE-02 | A cada push, o GitHub Actions sobe o docker-compose e roda a collection completa com o Newman, e termina verde. | CI | — | CI: Aceitação com a collection do Postman (ACE-02) |

## Fatia 2 · Tipo de usuário

| ID | Cenário | Nível | Testes | Collection e CI |
|---|---|---|---|---|
| TIP-01 | Tipo criado com o nome "Entregador" recebe o código `ENTREGADOR`. | U | `CadastrarTipoUsuarioUseCaseTest.deveCadastrarComOCodigoGerado`<br>`TipoUsuarioTest.deveGerarOCodigoAPartirDoNome` | Postman: 9. Cadastrar tipo |
| TIP-02 | O código é gerado sem acentos, em maiúsculas e com espaços trocados por sublinhado: "Ajudante de Cozinha" vira `AJUDANTE_DE_COZINHA`, "Gerência" vira `GERENCIA`. | U | `TipoUsuarioTest.deveGerarCodigoSemAcentosEmMaiusculas` | — |
| TIP-03 | Nome vazio ou fora de 3 a 50 caracteres é recusado. | U | `CadastrarTipoUsuarioUseCaseTest.deveRecusarNomeInvalido`<br>`TipoUsuarioTest.deveRecusarNomeCurtoOuAusente`<br>`TipoUsuarioTest.deveRecusarNomeLongoOuSemLetras` | Postman: 9. Cadastrar tipo com nome curto |
| TIP-04 | Renomear um tipo mantém o código. | U | `RenomearTipoUsuarioUseCaseTest.deveRenomearMantendoOCodigo`<br>`TipoUsuarioTest.deveManterOCodigoAoRenomear`<br>`TipoUsuarioTest.deveManterONomeAnterior_QuandoORenomeFalhar`<br>`JpaTipoUsuarioDataSourceIT.deveManterOCodigoNaAtualizacao` | Postman: 9. Renomear tipo |
| TIP-05 | `CLIENTE` e `DONO_RESTAURANTE` são reconhecidos como tipos de sistema pelo código. | U | `TipoUsuarioTest.deveReconhecerOsTiposDeSistemaPeloCodigo`<br>`TipoUsuarioTest.deveRecusarCodigoAusenteNaReconstituicao`<br>`TipoUsuarioTest.deveCompararTiposPeloId`<br>`TipoUsuarioGatewayTest.deveBuscarPorCodigo` | — |
| TIP-06 | Cadastro com nome já existente, mesmo com maiúsculas ou acentos diferentes, devolve conflito. | C · I | `CadastrarTipoUsuarioUseCaseTest.deveRecusarNomeJaCadastrado`<br>`TipoUsuarioApiIT.deveRecusarNomeRepetido`<br>`JpaTipoUsuarioDataSourceIT.deveCompararNomesSemMaiusculasNemAcentos`<br>`TipoUsuarioGatewayTest.deveRepassarVerificacoesEExclusao` | Postman: 9. Cadastrar tipo com nome repetido |
| TIP-07 | Cadastro cujo código gerado coincide com o de outro tipo devolve conflito. | C | `CadastrarTipoUsuarioUseCaseTest.deveRecusarCodigoJaUsado`<br>`TipoUsuarioApiIT.deveRecusarCodigoRepetido` | — |
| TIP-08 | Renomear para o nome de outro tipo devolve conflito; renomear tipo inexistente devolve não encontrado. | C | `RenomearTipoUsuarioUseCaseTest.deveRecusarNomeDeOutroTipo`<br>`RenomearTipoUsuarioUseCaseTest.deveRecusarTipoInexistente` | — |
| TIP-09 | Renomear um tipo de sistema é permitido e não muda o código. | C | `RenomearTipoUsuarioUseCaseTest.deveRenomearTipoDeSistema`<br>`TipoUsuarioApiIT.deveRenomearTipoDeSistema` | — |
| TIP-10 | Excluir tipo sem usuários ativos remove o registro. | C | `ExcluirTipoUsuarioUseCaseTest.deveExcluirTipoSemUsuarios`<br>`TipoUsuarioControllerTest.deveExcluir` | Postman: 11. Excluir o tipo |
| TIP-11 | Excluir tipo usado por usuário ativo devolve conflito, informando quantos usuários o usam. | C · I · P | `ExcluirTipoUsuarioUseCaseTest.deveRecusarTipoEmUso`<br>`TipoUsuarioApiIT.deveRecusarTipoEmUso`<br>`JpaUsuarioDataSourceIT.deveContarEBuscarSoUsuariosAtivosDoTipo` | Postman: 9. Excluir tipo em uso |
| TIP-12 | Excluir `CLIENTE` ou `DONO_RESTAURANTE` devolve conflito. | C · I · P | `ExcluirTipoUsuarioUseCaseTest.deveRecusarTipoDeSistema`<br>`TipoUsuarioApiIT.deveRecusarTipoDeSistema` | Postman: 9. Excluir tipo de sistema |
| TIP-13 | Excluir um tipo que só era usado por usuários removidos é permitido. | I | `TipoUsuarioApiIT.deveExcluirTipoDeUsuariosRemovidos` | — |
| TIP-14 | Excluir tipo inexistente devolve não encontrado. | C | `ExcluirTipoUsuarioUseCaseTest.deveRecusarTipoInexistente` | — |
| TIP-15 | Uma base recém-criada já tem os tipos Cliente e Dono de Restaurante, sem nenhum passo manual. | I | `InicializacaoIT.deveTerOsTiposDeSistema` | Postman: 9. Listar tipos |
| TIP-16 | CRUD HTTP: POST devolve 201 com o código e `Location`; GET lista paginada; GET por id 200 e 404; PUT 200, 404 e 409; DELETE 204, 404 e 409. | I · P | `BuscarTipoUsuarioUseCaseTest.deveBuscarPorId`<br>`BuscarTipoUsuarioUseCaseTest.deveListar`<br>`TipoUsuarioApiIT.deveCadastrarTipo`<br>`TipoUsuarioApiIT.deveRecusarNomeInvalido`<br>`TipoUsuarioApiIT.deveListarPaginado`<br>`TipoUsuarioApiIT.deveBuscarPorId`<br>`TipoUsuarioApiIT.deveRenomear`<br>`TipoUsuarioApiIT.deveExcluir`<br>`JpaTipoUsuarioDataSourceIT.deveListarEExcluir`<br>`TipoUsuarioControllerTest.deveCadastrar`<br>`TipoUsuarioControllerTest.deveConsultarERenomear`<br>`TipoUsuarioGatewayTest.deveConverterNasDemaisOperacoes`<br>`TipoUsuarioPresenterTest.deveMontarAResposta`<br>`TipoUsuarioPresenterTest.deveConverterAPagina` | Postman: 9. Cadastrar tipo<br>Postman: 9. Cadastrar tipo com nome curto<br>Postman: 9. Listar tipos<br>Postman: 9. Consultar tipo<br>Postman: 9. Consultar tipo inexistente<br>Postman: 9. Renomear tipo<br>Postman: 11. Excluir o tipo<br>Postman: 11. Consultar o tipo excluído |
| TIP-17 | GET `/api/v1/tipos-usuario/{id}/usuarios` lista, paginados, só os usuários ativos do tipo; tipo inexistente devolve 404. | I · P | `BuscarTipoUsuarioUseCaseTest.deveListarOsUsuariosDoTipo`<br>`TipoUsuarioApiIT.deveListarUsuariosDoTipo`<br>`JpaUsuarioDataSourceIT.deveContarEBuscarSoUsuariosAtivosDoTipo`<br>`TipoUsuarioControllerTest.deveListarUsuariosDoTipo`<br>`UsuarioGatewayTest.deveBuscarPorTipoEContar` | Postman: 9. Listar usuários do tipo |
| TIP-18 | A chave estrangeira do banco recusa a exclusão de um tipo referenciado, mesmo se a verificação do caso de uso for contornada. | I | `JpaTipoUsuarioDataSourceIT.deveBarrarExclusaoDeTipoEmUsoNoBanco` | — |
| TRO-01 | Trocar o tipo para Dono de Restaurante com CNPJ válido troca o tipo e o documento. | U · C | `TrocarTipoDoUsuarioUseCaseTest.deveTrocarClienteParaDono`<br>`UsuarioTest.deveTrocarParaDonoComCnpj`<br>`UsuarioControllerTest.deveTrocarOTipo` | Postman: 10. Trocar para Dono de Restaurante |
| TRO-02 | Trocar para Dono de Restaurante informando CPF é recusado. | U | `TrocarTipoDoUsuarioUseCaseTest.deveRecusarCpfParaDono`<br>`UsuarioTest.deveRecusarTrocaParaDonoComCpf` | Postman: 10. Trocar para Dono informando CPF |
| TRO-03 | Trocar para Cliente, ou para um tipo criado pelo CRUD, informando CNPJ é recusado. | U | `UsuarioTest.deveRecusarTrocaParaOutrosTiposComCnpj`<br>`UsuarioTest.deveTrocarParaTipoCriadoPeloCrud` | — |
| TRO-04 | Trocar com documento já usado por outro usuário devolve conflito. | C | `TrocarTipoDoUsuarioUseCaseTest.deveRecusarDocumentoDeOutroUsuario`<br>`JpaUsuarioDataSourceIT.deveVerificarDocumentoEmOutroUsuario` | — |
| TRO-05 | Usuário inexistente ou removido, ou tipo inexistente, devolve não encontrado. | C | `TrocarTipoDoUsuarioUseCaseTest.deveRecusarUsuarioOuTipoInexistente` | Postman: 10. Trocar para tipo inexistente |
| TRO-06 | Dono sem restaurante ativo pode virar Cliente informando CPF. | C | `TrocarTipoDoUsuarioUseCaseTest.deveTrocarDonoParaCliente` | — |
| TRO-07 | Trocar para o tipo atual com o mesmo documento é aceito, sem alteração. | C | `TrocarTipoDoUsuarioUseCaseTest.deveAceitarTrocaSemMudanca`<br>`UsuarioTest.deveReconhecerOTipoEODocumentoAtuais`<br>`TrocaDeTipoApiIT.deveAceitarTrocaSemMudanca` | — |
| TRO-08 | PATCH `/api/v1/usuarios/{id}/tipo` devolve 200 com o novo tipo e o novo documento; documento incompatível devolve 400; usuário ou tipo inexistente, 404; documento em uso, 409. | I · P | `TrocarTipoDoUsuarioUseCaseTest.deveRecusarDocumentoAusenteOuInvalido`<br>`TrocaDeTipoApiIT.deveTrocarOTipo`<br>`TrocaDeTipoApiIT.deveRecusarDocumentoIncompativel`<br>`TrocaDeTipoApiIT.deveRecusarUsuarioOuTipoInexistente`<br>`TrocaDeTipoApiIT.deveRecusarDocumentoEmUso` | Postman: 10. Trocar para Dono informando CPF<br>Postman: 10. Trocar para tipo inexistente<br>Postman: 10. Trocar para Dono de Restaurante |
| TRO-09 | Depois da troca, o GET do usuário e a lista de usuários do tipo mostram o novo tipo. | I · P | `TrocaDeTipoApiIT.deveRefletirOTipoNovoNasConsultas` | Postman: 10. Consultar o usuário depois da troca<br>Postman: 10. Usuários do tipo antigo |

## Fatia 3 · Restaurante

| ID | Cenário | Nível | Testes | Collection e CI |
|---|---|---|---|---|
| RES-01 | Restaurante válido é criado com nome, endereço, tipo de cozinha, ao menos um turno e dono. | U | `RestauranteTest.deveCriarRestauranteValido`<br>`RestauranteTest.deveReconstituirComIdEDatas`<br>`RestauranteGatewayTest.deveConverterNosDoisSentidos` | — |
| RES-02 | Nome vazio ou fora de 2 a 120 caracteres é recusado. | U | `RestauranteTest.deveRecusarNomeInvalido`<br>`RestauranteTest.deveRespeitarOsLimitesDoNome` | — |
| RES-03 | Endereço, tipo de cozinha, horários ou dono ausente é recusado. | U | `CadastrarRestauranteUseCaseTest.deveRecusarSemEndereco`<br>`RestauranteTest.deveExigirOsCamposObrigatorios` | — |
| RES-04 | Alterar um campo para valor inválido falha, e o restaurante continua com o valor anterior. | U | `AtualizarRestauranteUseCaseTest.deveRecusarSemEndereco`<br>`RestauranteTest.deveManterOValorAnterior_QuandoAAlteracaoFalhar` | — |
| RES-05 | Cadastro com dono do tipo Dono de Restaurante é aceito. | C | `CadastrarRestauranteUseCaseTest.deveCadastrar` | — |
| RES-06 | Cadastro com dono inexistente ou removido devolve não encontrado. | C | `CadastrarRestauranteUseCaseTest.deveRecusarDonoInexistente` | — |
| RES-07 | Cadastro com dono do tipo Cliente devolve conflito, com mensagem que orienta a trocar o tipo antes. | C · I · P | `CadastrarRestauranteUseCaseTest.deveRecusarDonoCliente`<br>`RestauranteTest.deveExigirDonoDoTipoDonoDeRestaurante`<br>`RestauranteApiIT.deveRecusarDonoInvalido` | Postman: 12. Cadastrar restaurante com dono Cliente |
| RES-08 | A atualização altera os dados e substitui os horários. | C | `AtualizarRestauranteUseCaseTest.deveAtualizarESubstituirOsTurnos`<br>`RestauranteTest.deveAtualizarPelosSetters`<br>`RestauranteControllerTest.deveAtualizar`<br>`RestauranteGatewayTest.deveAtualizarEBuscar` | — |
| RES-09 | Transferir o restaurante para outro Dono de Restaurante é aceito; para um Cliente, devolve conflito. | C · I | `AtualizarRestauranteUseCaseTest.deveTransferirParaOutroDono`<br>`AtualizarRestauranteUseCaseTest.deveRecusarTransferenciaInvalida`<br>`RestauranteApiIT.deveTransferirODono` | — |
| RES-10 | Atualizar ou excluir restaurante inexistente ou removido devolve não encontrado. | C | `AtualizarRestauranteUseCaseTest.deveRecusarRestauranteInexistente`<br>`ExcluirRestauranteUseCaseTest.deveRecusarRestauranteInexistente` | — |
| RES-11 | Excluir um restaurante preenche `removido_em` e mantém a linha. | C · I | `ExcluirRestauranteUseCaseTest.deveRemoverLogicamente`<br>`RestauranteApiIT.deveExcluirLogicamente`<br>`RestauranteControllerTest.deveExcluir` | Postman: 12. Excluir restaurante |
| RES-12 | POST `/api/v1/restaurantes` devolve 201 com `Location`, o dono como `{id, nome}` e os turnos ordenados. | I · P | `RestauranteApiIT.deveCadastrar`<br>`RestauranteApiIT.naoDeveExporDadosInternos`<br>`RestauranteControllerTest.deveCadastrar`<br>`RestaurantePresenterTest.deveMontarAResposta` | Postman: 12. Cadastrar restaurante<br>Postman: 14. Cadastrar o bistrô<br>Postman: 14. Cadastrar a lanchonete |
| RES-13 | GET `/api/v1/restaurantes` devolve página com filtro por nome (contém, ignorando maiúsculas) e por tipo de cozinha. | I · P | `BuscarRestaurantesUseCaseTest.deveListarComFiltros`<br>`BuscarRestaurantesUseCaseTest.deveListarSemFiltros`<br>`RestauranteApiIT.deveListarComFiltros`<br>`RestauranteControllerTest.deveConsultar`<br>`RestauranteGatewayTest.deveListar`<br>`RestaurantePresenterTest.deveConverterAPagina` | Postman: 12. Listar com filtro de nome |
| RES-14 | GET por id devolve 200; restaurante inexistente ou removido devolve 404. | I · P | `BuscarRestaurantesUseCaseTest.deveBuscarPorId`<br>`RestauranteApiIT.deveBuscarPorId`<br>`RestauranteControllerTest.deveConsultar` | Postman: 12. Consultar restaurante |
| RES-15 | PUT devolve 200; `dataCriacao` não muda e `dataUltimaAlteracao` avança. | I · P | `RestauranteApiIT.deveAtualizar` | Postman: 12. Atualizar restaurante |
| RES-16 | DELETE devolve 204; depois disso, o restaurante some do GET e da listagem. | I · P | `RestauranteApiIT.deveExcluirLogicamente` | Postman: 12. Excluir restaurante<br>Postman: 12. Consultar restaurante excluído<br>Postman: 15. Excluir o bistrô<br>Postman: 15. Excluir a lanchonete |
| RES-17 | GET `/api/v1/usuarios/{id}/restaurantes` lista, paginados, os restaurantes ativos do usuário. | I · P | `BuscarRestaurantesUseCaseTest.deveListarRestaurantesDoUsuario`<br>`RestauranteApiIT.deveListarOsRestaurantesDoUsuario`<br>`UsuarioControllerTest.deveListarOsRestaurantesDoUsuario`<br>`RestauranteGatewayTest.deveListar` | Postman: 12. Restaurantes da dona |
| RES-18 | Listar restaurantes com o nome do dono executa um número fixo de consultas, sem uma consulta extra por restaurante (N+1). | I | `ConsultasDeRestauranteIT.deveListarSemConsultaExtraPorRestaurante` | — |
| HOR-01 | Turno com dia, abertura e fechamento válidos é aceito. | U | `DiaSemanaTest.deveTerOsDiasEmPortugues`<br>`TurnoTest.deveAceitarTurnoValido`<br>`TurnoTest.deveExigirTodosOsCampos` | — |
| HOR-02 | Turno com abertura igual ao fechamento é recusado. | U | `TurnoTest.deveRecusarAberturaIgualAoFechamento` | — |
| HOR-03 | Turno com fechamento antes da abertura termina no dia seguinte e é aceito (sexta 18:00–02:00). | U | `TurnoTest.deveAceitarTurnoQuePassaDaMeiaNoite` | — |
| HOR-04 | Dois turnos no mesmo dia sem sobreposição são aceitos (11:00–15:00 e 18:00–23:00). | U | `QuadroDeHorariosTest.deveAceitarEOrdenarTurnos`<br>`TurnoTest.deveCompararTurnosDoMesmoDia` | — |
| HOR-05 | Turnos com sobreposição parcial são recusados, com mensagem que cita os dois turnos. | U | `CadastrarRestauranteUseCaseTest.deveRecusarTurnosSobrepostos`<br>`QuadroDeHorariosTest.deveRecusarSobreposicaoParcial`<br>`TurnoTest.deveCompararTurnosDoMesmoDia`<br>`RestauranteApiIT.deveRecusarTurnosSobrepostos` | — |
| HOR-06 | Turno contido dentro de outro é recusado. | U | `QuadroDeHorariosTest.deveRecusarTurnoContido`<br>`TurnoTest.deveTratarTurnoContidoETurnosQueEncostam` | — |
| HOR-07 | Turnos que encostam (11:00–15:00 e 15:00–18:00) são aceitos. | U | `QuadroDeHorariosTest.deveAceitarTurnosQueEncostam`<br>`TurnoTest.deveTratarTurnoContidoETurnosQueEncostam` | — |
| HOR-08 | Turno da madrugada que colide com a parte depois da meia-noite do turno da noite anterior é recusado (sexta 18:00–02:00 e sábado 01:00–10:00). | U | `QuadroDeHorariosTest.deveRecusarColisaoDepoisDaMeiaNoite`<br>`TurnoTest.deveConsiderarAParteDepoisDaMeiaNoite` | Postman: 12. Cadastrar com turnos sobrepostos |
| HOR-09 | Turno de domingo à noite que colide com segunda de madrugada é recusado (domingo 22:00–03:00 e segunda 02:00–06:00). | U | `QuadroDeHorariosTest.deveRecusarColisaoNaVoltaDaSemana`<br>`TurnoTest.deveDarAVoltaNaSemana` | — |
| HOR-10 | Lista de turnos vazia é recusada. | U | `CadastrarRestauranteUseCaseTest.deveRecusarSemTurnos`<br>`QuadroDeHorariosTest.deveRecusarListaVazia`<br>`QuadroDeHorariosTest.deveGuardarUmaCopiaImutavel`<br>`RestauranteApiIT.deveExigirTurnosETipoDeCozinha` | — |
| HOR-11 | A resposta traz os turnos ordenados por dia e por horário de abertura. | U · I | `QuadroDeHorariosTest.deveAceitarEOrdenarTurnos`<br>`TurnoTest.deveOrdenarPorDiaEAbertura`<br>`RestauranteApiIT.deveCadastrar` | Postman: 12. Cadastrar restaurante |
| HOR-12 | O PUT do restaurante substitui todos os turnos: os antigos deixam de existir no banco. | I | `RestauranteApiIT.deveAtualizar`<br>`RestauranteApiIT.deveRegistrarAlteracaoSoDosTurnos` | Postman: 12. Atualizar restaurante |
| HOR-13 | Horário em formato inválido ("25:00", "11h") ou dia inexistente devolve 400. | I | `CadastrarRestauranteUseCaseTest.deveRecusarHorarioInvalido`<br>`DiaSemanaTest.deveRecusarDiaInexistente`<br>`RestauranteApiIT.deveRecusarHorarioOuDiaInvalido` | — |
| COZ-01 | Tipo de cozinha fora da lista devolve 400, com os valores aceitos na mensagem. | U · I | `CadastrarRestauranteUseCaseTest.deveRecusarTipoDeCozinhaInvalido`<br>`TipoCozinhaTest.deveAceitarValoresDaLista`<br>`TipoCozinhaTest.deveRecusarValorForaDaLista`<br>`RestauranteApiIT.deveRecusarTipoDeCozinhaInvalido` | Postman: 12. Cadastrar com tipo de cozinha inválido |
| COZ-02 | Tipo de cozinha ausente devolve 400. | I | `TipoCozinhaTest.deveRecusarValorAusente`<br>`RestauranteApiIT.deveExigirTurnosETipoDeCozinha` | — |
| COZ-03 | O filtro por tipo de cozinha devolve só os restaurantes daquele tipo; valor inválido no filtro devolve 400. | I · P | `BuscarRestaurantesUseCaseTest.deveRecusarFiltroDeCozinhaInvalido`<br>`RestauranteApiIT.deveListarComFiltros` | Postman: 12. Listar com filtro de tipo de cozinha<br>Postman: 12. Listar com tipo de cozinha inválido |
| EXC-07 | Excluir usuário responsável por restaurante ativo devolve 409 e não altera nada. | C · I · P | `ExcluirUsuarioUseCaseTest.deveRecusarUsuarioComRestauranteAtivo`<br>`RegrasDoDonoApiIT.deveRecusarExclusaoDeDonoComRestauranteAtivo`<br>`RestauranteGatewayTest.deveRepassarContagemERemocao` | Postman: 12. Excluir a dona com restaurante ativo |
| EXC-08 | Excluir usuário cujos restaurantes estão todos removidos é permitido, e esses restaurantes continuam apontando para o id dele. | I | `RegrasDoDonoApiIT.deveExcluirDonoSemRestauranteAtivo` | Postman: 13. Excluir a dona<br>Postman: 15. Excluir a dona |
| TRO-10 | Dono com restaurante ativo não pode virar Cliente: devolve conflito, com orientação para transferir ou excluir o restaurante. | C · I · P | `TrocarTipoDoUsuarioUseCaseTest.deveRecusarDonoComRestauranteAtivo`<br>`TrocarTipoDoUsuarioUseCaseTest.naoDeveContarRestaurantesQuandoContinuaDono`<br>`RegrasDoDonoApiIT.deveRecusarTrocaDeDonoComRestauranteAtivo` | Postman: 12. Trocar a dona para Cliente com restaurante ativo |
| TRO-11 | Dono cujos restaurantes estão todos removidos pode virar Cliente. | C | `TrocarTipoDoUsuarioUseCaseTest.deveTrocarDonoParaCliente`<br>`RegrasDoDonoApiIT.deveTrocarDonoSemRestauranteAtivo` | — |

## Fatia 4 · Item do cardápio

| ID | Cenário | Nível | Testes | Collection e CI |
|---|---|---|---|---|
| ITE-01 | Item válido é criado com nome, descrição, preço, disponibilidade só no local e caminho da foto. | U | `ItemCardapioTest.deveCriarItemValido`<br>`ItemCardapioTest.deveReconstituirComIdEDatas`<br>`ItemCardapioTest.deveExigirPrecoDisponibilidadeERestaurante`<br>`PrecoTest.deveExigirOPreco`<br>`ItemCardapioGatewayTest.deveConverterNosDoisSentidos` | — |
| ITE-02 | Nome fora de 2 a 100 caracteres, ou descrição vazia ou acima de 500, é recusado. | U | `ItemCardapioTest.deveRecusarNomeInvalido`<br>`ItemCardapioTest.deveRecusarNomeLongoDemais`<br>`ItemCardapioTest.deveRecusarDescricaoInvalida`<br>`ItemCardapioTest.deveManterOValorAnterior_QuandoAAlteracaoFalhar` | — |
| ITE-03 | Espaços sobrando no nome são removidos: "  Feijoada   completa " vira "Feijoada completa". | U | `CadastrarItemCardapioUseCaseTest.deveCadastrar`<br>`ItemCardapioTest.deveTirarEspacosSobrando` | Postman: 14. Cadastrar item |
| ITE-04 | Preço zero ou negativo é recusado. | U | `PrecoTest.deveRecusarPrecoZeroOuNegativo` | — |
| ITE-05 | Preço com mais de duas casas decimais é recusado, sem arredondar. | U | `CadastrarItemCardapioUseCaseTest.deveRecusarPrecoInvalido`<br>`PrecoTest.deveRecusarMaisDeDuasCasas`<br>`ItemCardapioApiIT.deveRecusarCamposInvalidos` | — |
| ITE-06 | Preço acima de 99.999.999,99 é recusado. | U | `PrecoTest.deveRespeitarOLimite` | — |
| ITE-07 | Preço com uma casa decimal é guardado e devolvido com duas (39.9 vira 39.90). | U · I | `PrecoTest.deveGuardarComDuasCasas`<br>`PrecoTest.deveAceitarZerosADireita`<br>`ItemCardapioApiIT.deveCadastrar`<br>`ItemCardapioPresenterTest.deveMontarAResposta` | Postman: 14. Cadastrar item |
| ITE-08 | Caminho da foto ausente, acima de 255 caracteres ou sem extensão `.jpg`, `.jpeg`, `.png` ou `.webp` é recusado; caminho relativo e URL são aceitos. | U | `ItemCardapioTest.deveAceitarCaminhosDeFoto`<br>`ItemCardapioTest.deveRecusarFotoSemExtensaoDeImagem`<br>`ItemCardapioTest.deveRecusarFotoAusenteOuLongaDemais` | — |
| ITE-09 | Disponibilidade só no local ausente devolve 400, sem assumir `false`. | I | `ItemCardapioApiIT.deveRecusarCamposInvalidos` | Postman: 14. Cadastrar item sem a disponibilidade |
| ITE-10 | Cadastro em restaurante ativo é aceito. | C | `CadastrarItemCardapioUseCaseTest.deveCadastrar` | — |
| ITE-11 | Cadastro em restaurante inexistente ou removido devolve não encontrado. | C | `CadastrarItemCardapioUseCaseTest.deveRecusarRestauranteInexistente` | — |
| ITE-12 | Cadastro com o nome de outro item ativo do mesmo restaurante devolve conflito. | C · I · P | `CadastrarItemCardapioUseCaseTest.deveRecusarNomeRepetido`<br>`ItemCardapioApiIT.deveRecusarNomeRepetido` | Postman: 14. Cadastrar item com nome repetido |
| ITE-13 | O mesmo nome com maiúsculas, acentos ou espaço no fim diferentes também devolve conflito. | I | `ItemCardapioApiIT.deveRecusarNomeRepetido` | Postman: 14. Cadastrar item com nome repetido |
| ITE-14 | O mesmo nome em outro restaurante é aceito. | C · I | `CadastrarItemCardapioUseCaseTest.deveAceitarMesmoNomeEmOutroRestaurante`<br>`ItemCardapioApiIT.deveAceitarMesmoNomeEmOutroRestaurante` | Postman: 14. Cadastrar o mesmo nome em outro restaurante |
| ITE-15 | Atualizar um item sem mudar o nome é aceito: a verificação ignora o próprio item. | C · I | `AlterarItemCardapioUseCaseTest.deveAtualizarMantendoONome`<br>`ItemCardapioApiIT.deveConsultarEAtualizar` | Postman: 14. Atualizar item mantendo o nome |
| ITE-16 | Renomear para o nome de outro item ativo devolve conflito. | C | `AlterarItemCardapioUseCaseTest.deveRecusarNomeDeOutroItem`<br>`ItemCardapioApiIT.deveRecusarRenomearParaNomeDeOutroItem` | — |
| ITE-17 | Acessar, pela URL de um restaurante, um item que pertence a outro devolve 404. | C · I | `AlterarItemCardapioUseCaseTest.deveRecusarAtualizacaoPorOutroRestaurante`<br>`AlterarItemCardapioUseCaseTest.deveRecusarExclusaoPorOutroRestaurante`<br>`ConsultarItensCardapioUseCaseTest.deveRecusarItemDeOutroRestaurante`<br>`ItemCardapioApiIT.deveEsconderItemDeOutroRestaurante` | Postman: 14. Consultar item pela rota de outro restaurante |
| ITE-18 | Excluir um item preenche `removido_em` e mantém a linha. | C · I | `AlterarItemCardapioUseCaseTest.deveExcluirLogicamente`<br>`ItemCardapioApiIT.deveExcluirLogicamente` | Postman: 14. Excluir item<br>Postman: 15. Excluir o item novo<br>Postman: 15. Excluir o item da lanchonete |
| ITE-19 | Depois de excluir um item, criar outro com o mesmo nome é aceito. | I · P | `ItemCardapioApiIT.deveExcluirLogicamente`<br>`JpaItemCardapioDataSourceIT.deveLiberarONomeDoItemRemovido` | Postman: 14. Recadastrar com o mesmo nome |
| ITE-20 | O banco recusa dois itens ativos com o mesmo nome no mesmo restaurante, mesmo se a verificação do caso de uso for contornada. | I | `JpaItemCardapioDataSourceIT.deveBarrarNomeRepetidoNoBanco`<br>`JpaItemCardapioDataSourceIT.deveLiberarONomeDoItemRemovido` | — |
| ITE-21 | CRUD HTTP aninhado: POST devolve 201 com `Location`; GET lista paginada com filtro `apenasNoLocal`; GET por id; PUT; DELETE devolve 204 e, depois dele, 404. | I · P | `ConsultarItensCardapioUseCaseTest.deveBuscarPorId`<br>`ConsultarItensCardapioUseCaseTest.deveListar`<br>`ItemCardapioApiIT.deveCadastrar`<br>`ItemCardapioApiIT.deveListar`<br>`ItemCardapioApiIT.deveConsultarEAtualizar`<br>`ItemCardapioControllerTest.deveCadastrarEConsultar`<br>`ItemCardapioControllerTest.deveAtualizarEExcluir`<br>`ItemCardapioGatewayTest.deveConsultarERepassar`<br>`ItemCardapioPresenterTest.deveConverterAPagina` | Postman: 14. Cadastrar item<br>Postman: 14. Listar o cardápio<br>Postman: 14. Consultar item<br>Postman: 14. Atualizar item mantendo o nome<br>Postman: 14. Excluir item<br>Postman: 14. Consultar item excluído |
| ITE-22 | Todas as rotas de item de um restaurante removido devolvem 404. | I | `ConsultarItensCardapioUseCaseTest.deveRecusarRestauranteRemovido`<br>`ItemCardapioApiIT.deveEsconderItensDeRestauranteRemovido`<br>`RestauranteGatewayTest.deveRepassarContagemERemocao` | Postman: 15. Cardápio de restaurante excluído |

## Fatia 5 · Fechamento

| ID | Cenário | Nível | Testes | Collection e CI |
|---|---|---|---|---|
