# CampusGigs API

API REST para alunos publicarem e contratarem serviços. O projeto foi escrito como base para o enunciado “Projeto Diamante 1 - CampusGigs”. Em 16/09/2026, o código foi compilado com Java 21, os 12 testes automatizados foram aprovados e o fluxo manual foi executado com PostgreSQL e Docker. Os resultados estão registrados na pasta docs.

## Executar

Pré-requisito: Docker Desktop/Engine com Compose. Copie `.env.example` para `.env` e substitua `CAMPUSGIGS_JWT_SECRET` por uma sequência aleatória de pelo menos 32 bytes. O valor do exemplo serve apenas para demonstrar o formato. Opcionalmente configure `CAMPUSGIGS_ADMIN_EMAIL` e `CAMPUSGIGS_ADMIN_PASSWORD` (12+ caracteres) para provisionar um ADMIN de demonstração. Cadastro público sempre cria papel USER.

```bash
docker compose up --build
docker compose ps
docker compose logs api
```

A API usa `http://localhost:8080`. PostgreSQL e API sobem juntos; a migration `V1__initial_schema.sql` cria o schema. `spring.jpa.hibernate.ddl-auto=validate` só verifica as entidades. Para consultar o histórico:

```bash
docker compose exec db psql -U campusgigs -d campusgigs -c "select installed_rank, version, description from flyway_schema_history;"
```

O banco e senha do Compose são exclusivamente de desenvolvimento. Não publique `.env`, JWT, senhas reais nem dados pessoais nas evidências.

## Rotas e regras

| Método e rota | Token | Regra / resposta esperada |
| --- | --- | --- |
| `POST /api/auth/register` | não | cadastra USER; CEP consultado no ViaCEP; senha BCrypt; 201 |
| `POST /api/auth/login` | não | emite JWT Bearer; 200 |
| `PATCH /api/users/me/cep` | sim | altera CEP/cidade/UF do próprio usuário; 200 |
| `GET /api/services` | não | lista serviços publicados; 200 |
| `POST /api/services` | sim | publica como usuário do JWT; 201 |
| `PUT /api/services/{id}` | sim | somente dono; 403 para outro USER |
| `PATCH /api/services/{id}/status` | sim | dono pausa/reativa; encerrado não reativa |
| `PATCH /api/services/{id}/close` | sim | dono ou ADMIN; 403 para outro USER |
| `POST /api/services/{id}/contracts` | sim | contrata como usuário do JWT; 409 para serviço não ativo ou próprio |

Sem token ou com token inválido/expirado: 401. Usuário autenticado sem permissão: 403. Dados inválidos: 400. E-mail repetido e regras de estado: 409. CEP bem formatado mas inexistente: 422. ViaCEP indisponível/timeout: 503. Erros retornam JSON com `code`, `message` e `timestamp`, sem stack trace.

## Exemplo de chamada autenticada

Os exemplos abaixo funcionam em shell com `curl`. Execute cadastro e login; copie manualmente o `accessToken` recebido para o lugar de `SEU_TOKEN`.

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ana","email":"ana@campus.test","password":"SenhaForte123!","cep":"01001000"}'

curl -i -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@campus.test","password":"SenhaForte123!"}'

curl -i -X POST http://localhost:8080/api/services \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer SEU_TOKEN' \
  -d '{"title":"Revisão de Java","description":"Aula de revisão","category":"EDUCACAO","price":40.00}'
```

Crie outro USER, B, para testar contratação. B usa seu próprio token em `POST /api/services/1/contracts`; a resposta deve mostrar `customerId` de B. Depois tente `PATCH /api/services/1/close` com token de B: deve ser 403. Com token de A, `POST /api/services/1/contracts` deve ser 409 (`SELF_HIRE`). Encerre com A ou ADMIN; contratar após o encerramento deve ser 409 (`SERVICE_NOT_ACTIVE`). Faça também chamada protegida sem token e com token adulterado: 401. IDs podem mudar; copie o `id` real da publicação.

Com a API rodando, o PowerShell pode executar quase todo esse roteiro e gerar `docs/evidencia-manual.json` sem gravar senhas ou tokens:

```powershell
.\scripts\demo.ps1
# Inclui o caso ADMIN quando o provisionamento opcional foi configurado:
.\scripts\demo.ps1 -AdminEmail 'admin@campus.test' -AdminPassword 'sua-senha-admin'
```

Revise o JSON gerado, faça capturas das respostas importantes e não publique segredos. O script consulta o ViaCEP de verdade, então precisa de internet.

## Código que vale abrir na apresentação técnica

1. `src/main/resources/db/migration/V1__initial_schema.sql` e `compose.yaml`: schema, FKs, e-mail único e ambiente reproduzível.
2. `AuthService.java` e `SecurityConfig.java`: BCrypt, emissão HS256, assinatura/expiração, proteção de endpoints.
3. `OfferService.java` e `ContractService.java`: dono, ADMIN, estado ativo e contratação própria. O lock pessimista serializa contratação com alteração de status.
4. `ViaCepClient.java`, `ViaCepConfig.java` e `AddressLookup.java`: `@GetExchange`, proxy declarativo, timeout e distinção entre CEP inexistente e falha externa.
5. `GlobalExceptionHandler.java`: respostas de erro sem revelar exceções internas.

## Testes e checkpoints

Os testes Maven devem ser executados com Java/Maven local; o Dockerfile compila o projeto, mas pula os testes durante a criação da imagem. Se houver Maven instalado:

```bash
mvn test
```

Os 12 testes incluídos cobrem propriedade, ADMIN, contratação própria, serviço encerrado, senha protegida, assinatura JWT e CEP inexistente/timeout. Os testes de integração sobem o contexto Spring, aplicam a migration em H2 compatível com PostgreSQL e percorrem o fluxo HTTP de cadastro, login, JWT, publicação, contratação, 401, 403 e 409. O resultado desta execução está em `docs/validacao-automatica.md`. O workflow `.github/workflows/tests.yml` executa `mvn verify` no GitHub. Complete com uma execução manual usando PostgreSQL/Docker, documentada em `docs/testes-manuais.md`; os casos precisam ser observados de verdade antes da entrega.

O enunciado exige cinco commits de checkpoint com justificativa curta de 1 a 3 linhas nas suas palavras. Este pacote é um estado inicial de código, não um histórico de desenvolvimento. Organize CP1 a CP5 enquanto você implementa e testa, sem fabricar commits retroativos. Confira `git log --oneline`, publique o repositório no GitHub e entregue seu link no Geminidev.

Documentação técnica usada: [Spring Boot 3.5 e Flyway](https://docs.spring.io/spring-boot/3.5/how-to/data-initialization.html), [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html), [HTTP Interface Spring](https://docs.spring.io/spring-framework/reference/6.2/integration/rest-clients.html), [ViaCEP](https://viacep.com.br/).
