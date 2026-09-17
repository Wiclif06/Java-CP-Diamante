# Passo a passo final da entrega CampusGigs

Este roteiro cobre as tarefas que precisam ser executadas na sua sessão do Windows ou nas suas contas. Não marque um teste como aprovado sem observar o resultado real.

## 1 Iniciar o Docker

1. Clique com o botão direito no Docker Desktop e escolha Executar como administrador.
2. Aceite a janela de permissão do Windows.
3. Espere aparecer Engine running.
4. Se o Docker informar problema com WSL 2, reinicie o computador e abra o Docker novamente como administrador.

## 2 Abrir o projeto

Use a pasta onde você extraiu ou clonou o repositório CampusGigs.

Ela já contém um arquivo .env local com contas exclusivamente de demonstração. O .gitignore impede que esse arquivo seja enviado ao GitHub. Mesmo assim, confirme que .env não aparece no git status.

## 3 Subir e conferir o ambiente

Abra o PowerShell nessa pasta e execute, um comando por vez:

    docker --version
    docker compose version
    docker compose config --services
    docker compose up --build -d
    docker compose ps
    docker compose logs api --tail 100
    docker compose exec db psql -U campusgigs -d campusgigs -c "select installed_rank, version, description, success from flyway_schema_history;"

O resultado deve mostrar os serviços db e api, o banco como healthy, a API na porta configurada e a migration V1 com success igual a t. Nesta máquina, a porta local é 8081 porque outro projeto já usa 8080.

## 4 Executar os testes manuais

    Set-ExecutionPolicy -Scope Process Bypass
    .\scripts\demo.ps1 -ApiBase 'http://localhost:8081' -AdminEmail 'admin@campus.test' -AdminPassword '<SENHA_ADMIN_LOCAL>'
    Get-Content .\docs\evidencia-manual.json

Todas as linhas devem começar com [OK]. O script inclui o caso de token adulterado. Confira principalmente os resultados 401, 403, 409, 422 e o ADMIN com 200.

Os testes automatizados serão executados pelo GitHub Actions com mvn verify. O Dockerfile atual monta a aplicação com -DskipTests, portanto o build do contêiner não substitui essa confirmação.

## 5 Registrar evidências

Crie docs\prints e salve capturas legíveis de:

1. Docker em execução e versões.
2. Arquivos principais do projeto.
3. Build concluído, contêineres ativos e migration aplicada.
4. Linhas [OK] do script.
5. JSON com o 403 de B tentando encerrar o serviço de A.
6. Casos 401 e 409.
7. ADMIN encerrando o serviço de A com 200.
8. GitHub Actions verde, histórico de commits e README renderizado.

Não mostre o conteúdo de .env, senhas nem JWT completo. Atualize docs\testes-manuais.md com o resultado observado e o nome de cada captura.

## 6 Preparar os checkpoints Git

O professor exige cinco commits reais. Se você ainda não tem o histórico, não finja datas ou trabalho anterior. Organize as alterações reais que ainda fizer em commits coerentes:

- CP1: Docker e migration.
- CP2: cadastro, login e BCrypt.
- CP3: emissão e validação do JWT.
- CP4: propriedade, papéis e respostas 401 e 403.
- CP5: ViaCEP, timeout, erros e validação final.

Cada mensagem deve incluir uma justificativa verdadeira de 1 a 3 linhas nas suas palavras. Antes do primeiro commit:

    git init
    git branch -M main
    git status

Confirme que .env e target não aparecem. Depois use git add e git commit somente para o conteúdo correspondente a cada checkpoint.

## 7 Publicar e entregar

1. Crie um repositório vazio no GitHub, sem README, .gitignore ou licença.
2. Copie a URL do repositório.
3. No PowerShell, conecte a pasta com git remote add origin URL_DO_REPOSITORIO.
4. Execute git push -u origin main.
5. Aguarde o GitHub Actions terminar em verde.
6. Confira README, arquivos, cinco checkpoints e ausência do .env.
7. Envie o link do GitHub no Geminidev.

## 8 Finalizar

Depois de terminar as evidências, execute docker compose down.

Não use docker compose down -v a menos que queira apagar os dados de teste.
