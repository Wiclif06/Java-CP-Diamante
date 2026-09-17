# Evidência manual

Execução realizada em 16/09/2026 contra a API e o PostgreSQL reais em Docker. O resultado estruturado, sem senhas e sem tokens, está em docs/evidencia-manual.json.

| Caso | Esperado | Resultado observado |
| --- | --- | --- |
| Cadastro com CEP válido | 201, cidade e UF | aprovado: 201; confira cidade e UF na apresentação |
| Login | 200, accessToken | aprovado: 200 |
| Publicar como A | 201, providerId A | aprovado: 201 |
| Contratar como B | 201, customerId B | aprovado: 201 |
| Alteração sem token | 401 | aprovado: 401 |
| Token adulterado | 401 | aprovado: 401 |
| Negado por propriedade | 403 | aprovado: 403 |
| Contratar o próprio serviço | 409, SELF_HIRE | aprovado: 409 |
| Contratar serviço encerrado | 409, SERVICE_NOT_ACTIVE | aprovado: 409 |
| ADMIN encerra serviço de A | 200 | aprovado: 200 |
| CEP inexistente | 422, CEP_NOT_FOUND | aprovado: 422 |
| Indisponibilidade ou timeout real do ViaCEP | 503, CEP_SERVICE_UNAVAILABLE | não provocado manualmente; coberto por teste automatizado |

Ainda é necessário salvar as capturas de tela em docs/prints, sem senha, JWT completo ou dados pessoais reais.
