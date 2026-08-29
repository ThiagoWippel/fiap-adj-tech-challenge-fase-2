#!/usr/bin/env bash
# =========================================================
# Verificação dos endpoints da API
#
# Uso:
#   chmod +x verificar-api.sh
#   ./verificar-api.sh
#
# Repetível: captura os identificadores das respostas em vez de
# presumi-los, e remove ao final os registros que criou. Funciona
# tanto no perfil dev (H2, recriado a cada inicialização) quanto
# no perfil docker (MySQL, com volume persistente).
#
# Caso uma execução seja interrompida no meio, pode restar resíduo
# no banco e o cadastro inicial falhará com 409. Para limpar:
#   docker compose down -v && docker compose up --build
# =========================================================

BASE="http://localhost:8080"
VERDE='\033[0;32m'; VERMELHO='\033[0;31m'; CINZA='\033[0;90m'; FIM='\033[0m'

total=0
falhas=0
ultimo_corpo=""

# executa uma requisição e compara o status obtido com o esperado
verificar() {
    local descricao="$1" esperado="$2" metodo="$3" rota="$4" corpo="$5"
    total=$((total + 1))

    if [ -n "$corpo" ]; then
        resposta=$(curl -s -w "\n%{http_code}" -X "$metodo" "$BASE$rota" \
            -H "Content-Type: application/json" -d "$corpo")
    else
        resposta=$(curl -s -w "\n%{http_code}" -X "$metodo" "$BASE$rota")
    fi

    obtido=$(echo "$resposta" | tail -n1)
    ultimo_corpo=$(echo "$resposta" | sed '$d')

    if [ "$obtido" = "$esperado" ]; then
        printf "${VERDE}  OK  ${FIM} %-3s %-48s %s\n" "$obtido" "$descricao" "$rota"
    else
        falhas=$((falhas + 1))
        printf "${VERMELHO} FALHA${FIM} esperado %s, obtido %s - %s\n" "$esperado" "$obtido" "$descricao"
        printf "${CINZA}       %s${FIM}\n" "$(echo "$ultimo_corpo" | head -c 300)"
    fi
}

# extrai o campo id do último corpo de resposta
extrair_id() {
    echo "$ultimo_corpo" | grep -o '"id":[0-9]*' | head -n1 | cut -d: -f2
}

# confirma a presença de um campo no último corpo de resposta
verificar_campo() {
    local descricao="$1" campo="$2"
    total=$((total + 1))
    if echo "$ultimo_corpo" | grep -q "\"$campo\""; then
        printf "${VERDE}  OK  ${FIM} --  %s\n" "$descricao"
    else
        falhas=$((falhas + 1))
        printf "${VERMELHO} FALHA${FIM} campo ausente '%s' - %s\n" "$campo" "$descricao"
    fi
}

# confirma a AUSÊNCIA de um campo no último corpo de resposta
verificar_ausencia() {
    local descricao="$1" campo="$2"
    total=$((total + 1))
    if echo "$ultimo_corpo" | grep -q "\"$campo\""; then
        falhas=$((falhas + 1))
        printf "${VERMELHO} FALHA${FIM} campo indevido '%s' - %s\n" "$campo" "$descricao"
    else
        printf "${VERDE}  OK  ${FIM} --  %s\n" "$descricao"
    fi
}

CLIENTE='{
  "nome": "Maria Silva", "email": "maria.silva@exemplo.com", "login": "maria.silva",
  "senha": "SenhaSegura123", "tipo": "CLIENTE", "cpf": "123.456.789-09",
  "endereco": { "rua": "Rua das Flores", "numero": "123", "complemento": "Apto 45",
    "bairro": "Centro", "cidade": "Camboriu", "estado": "sc", "cep": "88340-000" }
}'

DONO='{
  "nome": "Joao Pereira", "email": "joao.pereira@exemplo.com", "login": "joao.pereira",
  "senha": "OutraSenha456", "tipo": "DONO_RESTAURANTE", "cnpj": "11.222.333/0001-81",
  "endereco": { "rua": "Avenida Brasil", "numero": "S/N",
    "bairro": "Nacoes", "cidade": "Balneario Camboriu", "estado": "SC", "cep": "88330100" }
}'

echo
echo "=== Cadastro ==="
verificar "Cliente valido"                     201 POST "/api/v1/usuarios" "$CLIENTE"
ID_CLIENTE=$(extrair_id)
verificar_ausencia "Resposta nao expoe a senha" senha

verificar "Dono de restaurante valido"         201 POST "/api/v1/usuarios" "$DONO"
ID_DONO=$(extrair_id)

verificar "E-mail duplicado"                   409 POST "/api/v1/usuarios" "$CLIENTE"

verificar "Campos invalidos"                   400 POST "/api/v1/usuarios" \
    '{"nome":"Ab","email":"invalido","login":"x","senha":"123","tipo":"CLIENTE"}'
verificar_campo "Erro traz a relacao de campos"     erros
verificar_campo "Erro traz o identificador de tipo" type
verificar_campo "Erro traz o instante da falha"     momento

verificar "CPF ausente para cliente"           400 POST "/api/v1/usuarios" \
    '{"nome":"Sem Documento","email":"sem.doc@exemplo.com","login":"sem.doc",
      "senha":"SenhaSegura123","tipo":"CLIENTE",
      "endereco":{"rua":"Rua A","numero":"1","bairro":"B","cidade":"C","estado":"SC","cep":"88000000"}}'

printf "${CINZA}       identificadores capturados: cliente=%s dono=%s${FIM}\n" "$ID_CLIENTE" "$ID_DONO"

echo
echo "=== Consulta e busca ==="
verificar "Consulta por id existente"          200 GET "/api/v1/usuarios/$ID_CLIENTE"
verificar "Consulta por id inexistente"        404 GET "/api/v1/usuarios/999999"
verificar "Busca com resultados"               200 GET "/api/v1/usuarios?nome=maria"
verificar "Busca sem resultados (lista vazia)" 200 GET "/api/v1/usuarios?nome=zzzzz"
verificar "Busca sem filtro"                   200 GET "/api/v1/usuarios"
verificar "Busca paginada (v2)"                200 GET "/api/v2/usuarios?nome=a&page=0&size=10"
verificar_campo "v2 devolve metadados de paginacao" totalPaginas

echo
echo "=== Atualizacao de dados ==="
ATUALIZACAO='{
  "nome": "Maria Silva Souza", "email": "maria.souza@exemplo.com", "login": "maria.souza",
  "endereco": { "rua": "Rua Nova", "numero": "456", "bairro": "Centro",
    "cidade": "Itajai", "estado": "SC", "cep": "88300000" }
}'
verificar "Atualizacao valida"                 200 PUT "/api/v1/usuarios/$ID_CLIENTE" "$ATUALIZACAO"
verificar "Atualizacao de inexistente"         404 PUT "/api/v1/usuarios/999999" "$ATUALIZACAO"
verificar "E-mail pertencente a outro"         409 PUT "/api/v1/usuarios/$ID_CLIENTE" \
    '{"nome":"Maria","email":"joao.pereira@exemplo.com","login":"maria.souza",
      "endereco":{"rua":"R","numero":"1","bairro":"B","cidade":"C","estado":"SC","cep":"88000000"}}'

echo
echo "=== Troca de senha ==="
verificar "Senha atual incorreta"              401 PUT "/api/v1/usuarios/$ID_CLIENTE/senha" \
    '{"senhaAtual":"SenhaErrada999","novaSenha":"SenhaNova456"}'
verificar "Nova senha fora das regras"         400 PUT "/api/v1/usuarios/$ID_CLIENTE/senha" \
    '{"senhaAtual":"SenhaSegura123","novaSenha":"123"}'
verificar "Troca valida"                       204 PUT "/api/v1/usuarios/$ID_CLIENTE/senha" \
    '{"senhaAtual":"SenhaSegura123","novaSenha":"SenhaNova456"}'

echo
echo "=== Autenticacao ==="
verificar "Senha antiga (deve falhar)"         401 POST "/api/v1/auth/login" \
    '{"login":"maria.souza","senha":"SenhaSegura123"}'
verificar "Credenciais validas"                200 POST "/api/v1/auth/login" \
    '{"login":"maria.souza","senha":"SenhaNova456"}'
verificar_ausencia "Login nao expoe o e-mail"  email
verificar "Login inexistente"                  401 POST "/api/v1/auth/login" \
    '{"login":"ninguem","senha":"QualquerCoisa1"}'

echo
echo "=== Exclusao ==="
verificar "Exclusao do dono"                   204 DELETE "/api/v1/usuarios/$ID_DONO"
verificar "Exclusao repetida"                  404 DELETE "/api/v1/usuarios/$ID_DONO"
verificar "Exclusao do cliente"                204 DELETE "/api/v1/usuarios/$ID_CLIENTE"

echo
echo "---------------------------------------------"
if [ "$falhas" -eq 0 ]; then
    printf "${VERDE}%s de %s verificacoes passaram${FIM}\n" "$total" "$total"
    printf "${CINZA}Base restaurada ao estado anterior - o script pode ser repetido.${FIM}\n\n"
else
    printf "${VERMELHO}%s de %s verificacoes falharam${FIM}\n\n" "$falhas" "$total"
fi

echo "=== Formato ProblemDetail (conferir visualmente) ==="
echo
curl -s -X POST "$BASE/api/v1/usuarios" -H "Content-Type: application/json" \
    -d '{"nome":"Ab","email":"invalido","login":"x","senha":"123","tipo":"CLIENTE"}' \
    | python3 -m json.tool 2>/dev/null || echo "(instale python3 para formatar a saida)"
echo

exit $falhas