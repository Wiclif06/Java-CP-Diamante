# Validação automática

Execução realizada em 16/09/2026 com Java 21.0.12.1 e Maven 3.9.11:

```text
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

O comando final foi `mvn verify`. A validação cobriu:

- compilação das classes principais e de teste;
- aplicação da migration `V1__initial_schema.sql` pelo Flyway;
- validação do mapeamento JPA com `ddl-auto=validate`;
- inicialização do contexto Spring;
- listagem pública de serviços;
- resposta 401 centralizada em operação sem token;
- fluxo HTTP completo de cadastro, login, publicação e contratação;
- respostas 403 por propriedade e 409 por regra de negócio;
- assinatura e validação do JWT;
- hash BCrypt no cadastro;
- autorização por dono e exceção ADMIN;
- contratação própria e serviço encerrado;
- CEP inválido, inexistente e falha do serviço externo.

Esta validação usa H2 em modo compatível com PostgreSQL para o teste de integração. Ela não substitui a execução manual com PostgreSQL via Docker Compose, exigida para comprovar o ambiente completo e os casos da apresentação.
