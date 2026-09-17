# Validação local do CampusGigs

Execução realizada em 16/09/2026 com Docker Desktop, Java 21 no contêiner, PostgreSQL 16 e Maven 3.9.

## Infraestrutura

- Imagem da API construída com sucesso.
- PostgreSQL iniciou com estado healthy.
- API iniciou com Spring Boot 3.5.16.
- Porta local usada nesta máquina: 8081, porque outro projeto já ocupava 8080.
- Flyway aplicou V1 initial schema com success igual a true.

## Testes automatizados

O comando Maven verify foi executado em um contêiner Java 21.

- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

## Fluxo manual

O script scripts/demo.ps1 foi executado contra o PostgreSQL real e a API em Docker. Foram aprovados 16 passos:

- cadastros A e B com CEP válido;
- login A e B;
- publicação autenticada;
- contratação por outro usuário;
- alteração sem token retorna 401;
- token adulterado retorna 401;
- tentativa de outro usuário encerrar o serviço retorna 403;
- contratação do próprio serviço retorna 409;
- encerramento pelo dono retorna 200;
- contratação de serviço encerrado retorna 409;
- CEP inexistente retorna 422;
- login ADMIN;
- segunda publicação;
- encerramento por ADMIN retorna 200.

Os resultados estruturados estão em docs/evidencia-manual.json. O caso de indisponibilidade ou timeout real do ViaCEP permanece coberto por teste automatizado, mas não foi provocado no ambiente manual.
