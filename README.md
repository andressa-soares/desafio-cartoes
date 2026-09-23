# desafio-cartoes

API REST que recebe os dados de um cliente e retorna os produtos de cartão de crédito
elegíveis ao seu perfil, de acordo com regras de renda, idade e UF.

## Sumário

- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Documentação da API](#documentação-da-api)
- [Exemplo de uso](#exemplo-de-uso)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Regras de negócio](#regras-de-negócio)
- [Configuração](#configuração)
- [Logs](#logs)
- [Testes](#testes)
- [Teste de carga](#teste-de-carga)
- [Documentação adicional](#documentação-adicional)

## Tecnologias

| Item | Versão |
|---|---|
| Java | 21 |
| Spring Boot (Web, Validation, Actuator) | 4.1.1 |
| Build | Maven (wrapper incluído, não precisa instalar Maven) |
| Documentação da API | OpenAPI 3.0 + springdoc (Swagger UI) |
| Container | Docker multistage |

## Pré-requisitos

- JDK 21
- Docker e Docker Compose (só se for rodar em container)

Não é necessário instalar Maven: o projeto usa o Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Como executar

### Local

```bash
./mvnw spring-boot:run
```

Windows (PowerShell/cmd):

```bash
mvnw.cmd spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

### Via Docker

```bash
docker compose up --build
```

Sobe o mesmo `http://localhost:8080`, em uma imagem multistage (build JDK descartado,
runtime só com JRE), rodando com usuário não-root e com healthcheck configurado em
`/actuator/health`.

## Documentação da API

Com a aplicação no ar:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Contrato OpenAPI (YAML): `http://localhost:8080/openapi.yaml`

O contrato é escrito à mão em `src/main/resources/openapi.yaml` e é a fonte única da
documentação — o Swagger UI aponta para ele em vez de gerar o contrato a partir de
anotações, para não correr o risco de duas versões divergentes.

## Exemplo de uso

```bash
curl -X POST http://localhost:8080/cartoes \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": {
      "nome": "Cliente Teste",
      "cpf": "123.456.789-10",
      "idade": 32,
      "data_nascimento": "1994-01-01",
      "uf": "SP",
      "renda_mensal": 8000.00,
      "email": "cliente@teste.com",
      "telefone_whatsapp": "11999992020"
    }
  }'
```

Resposta (`200`):

```json
{
  "numero_solicitacao": "745f2812-c3f4-42ce-93fb-e119e643bda2",
  "data_solicitacao": "2026-09-22T10:15:30.123",
  "cliente": { "...": "..." },
  "cartoes_ofertados": [
    {
      "tipo_cartao": "CARTAO_SEM_ANUIDADE",
      "valor_anuidade_mensal": 0.00,
      "valor_limite_disponivel": 1000.00,
      "status": "APROVADO"
    },
    {
      "tipo_cartao": "CARTAO_COM_CASHBACK",
      "valor_anuidade_mensal": 20.00,
      "valor_limite_disponivel": 5000.00,
      "status": "APROVADO"
    }
  ]
}
```

Todos os status possíveis (`200`, `204`, `400`, `415`, `422`, `500`) e seus payloads de
erro estão documentados no Swagger UI e na seção 6 do `docs/SDD.md`.

## Estrutura do projeto

```
br.com.desafio.cartoes
├── controller   # HTTP: DTOs em snake_case, mapper, tratamento de erros
├── service      # Orquestra o fluxo (filtro de renda → regras de perfil)
├── domain       # Modelos e regras de elegibilidade — sem dependência de framework
└── config       # application.yml → objetos de domínio, Swagger
```

Camadas com dependência em um único sentido: `domain` não importa `controller`,
`service` nem `config`. Detalhes e diagramas em `docs/SDD.md`.

## Regras de negócio

| Produto | Renda mínima | Limite | Anuidade mensal |
|---|---|---|---|
| CARTAO_SEM_ANUIDADE | R$ 3.500,00 | R$ 1.000,00 | R$ 0,00 |
| CARTAO_DE_PARCEIROS | R$ 5.500,00 | R$ 3.000,00 | R$ 10,00 |
| CARTAO_COM_CASHBACK | R$ 7.500,00 | R$ 5.000,00 | R$ 20,00 |

1. **Renda**: candidato a todo produto cuja renda mínima ele atinge. Nenhum atingido → `422`.
2. **Perfil**: 18 a 24 anos, só `CARTAO_SEM_ANUIDADE`; residente em SP, só
   `CARTAO_SEM_ANUIDADE` e `CARTAO_COM_CASHBACK`, exceto entre 25 e 29 anos (sem restrição).
3. Nenhum produto restante após os filtros de perfil → `204`.

Todos os valores de negócio vêm do `application.yml` (seção [Configuração](#configuração)),
nunca de literais no código. Regras completas, com bordas e exceções, em `docs/SDD.md`.

## Configuração

Parâmetros de negócio ficam em `src/main/resources/application.yml`, sob a chave `cartoes`:

```yaml
cartoes:
  idade-minima: 18
  faixa-jovem:
    inicio: 18
    fim: 24
  faixa-excecao-residente-sp:
    inicio: 25
    fim: 29
  produtos:
    - tipo: CARTAO_SEM_ANUIDADE
      renda-minima: 3500.00
      limite: 1000.00
      anuidade-mensal: 0.00
    # ...
```

Alterar um valor de negócio (renda mínima, faixa etária, limite) não exige mudança de
código — só editar o `application.yml` e reiniciar a aplicação.

## Logs

Só existe log de erro, de propósito (sem `INFO`): `WARN` para `4xx`, `ERROR` para `5xx`
com stack trace completo. CPF, e-mail, telefone e `data_nascimento` nunca aparecem em log,
mesma restrição da resposta HTTP. Detalhes do formato em `docs/SDD.md`, seção 6.1.

## Testes

```bash
./mvnw test
```

Roda testes unitários (`*Test.java`) e de integração ponta a ponta (`*IT.java`) juntos —
o Surefire está configurado para incluir os dois, sem precisar da fase `verify`.

| Nível | Cobre |
|---|---|
| Unitário (domínio) | Cada regra isolada, valores de borda |
| Unitário (service) | Fluxo 422/204/200 |
| Integração | `POST /cartoes` ponta a ponta, todos os status codes |

## Teste de carga

Script k6 em `performance/cartoes.test.js` (stress test, escala até 800 VUs em ~3m30s).
Com a aplicação no ar e o [k6](https://k6.io/docs/getting-started/installation/) instalado:

```bash
k6 run performance/cartoes.test.js
```

Gera `performance/summary.json`, com os resultados também impressos no terminal.

## Documentação adicional

Decisões de arquitetura, diagramas, regras de negócio completas e evolução do projeto
estão em [`docs/SDD.md`](docs/SDD.md).
