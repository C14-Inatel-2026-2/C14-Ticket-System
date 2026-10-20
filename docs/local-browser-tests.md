# Testes locais da criação de tickets

Requisitos: Java 21, Maven e navegador Edge instalado. O perfil `browser-tests` fixa Playwright Java 1.63.0. Não execute o Jenkinsfile para esta validação local: ele contém ações de publicação.

## Suíte Java

```powershell
mvn clean verify
```

## Formulário com respostas controladas

```powershell
$env:PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD='1'
mvn -Pbrowser-tests verify '-Dplaywright.channel=msedge'
```

O E2E fica desativado por padrão. Os testes de interface usam o HTML real e simulam apenas HTTP para reproduzir erros. Eles não comprovam persistência. A dependência e as fontes em `src/browser-test/java` são habilitadas pelo perfil.

## E2E com PostgreSQL real

Use uma instância descartável exclusiva, ligada somente a `127.0.0.1`, porta 55432, banco e usuário `c14_ticket_e2e`. Não reutilize o banco da aplicação. Crie o usuário com uma senha exclusiva para teste e permissão de criação das tabelas no banco. O teste força host, banco e usuário; apenas porta e senha podem ser configuradas. As migrations oficiais são executadas pelo Flyway. A aplicação inicia em porta HTTP aleatória e encerra ao terminar a JVM.

```powershell
$env:C14_E2E_DB_PORT='55432'
# Defina C14_E2E_DB_PASSWORD no ambiente com a senha exclusiva do banco de teste.
$env:PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD='1'
mvn -Pbrowser-tests verify '-Dc14.e2e=true' '-Dplaywright.channel=msedge'
```

O fluxo autentica pela tela com o login demonstrativo existente, verifica que responsável vazio não envia POST, cria um ticket exclusivo e confirma a lista após recarregar e a linha no banco. Ao terminar, remove somente o ticket com o identificador exclusivo dessa execução. As tabelas de teste permanecem no banco descartável.

Relatórios: `target/surefire-reports` (Java), `target/failsafe-reports` (navegador/E2E), `target/browser-artifacts` (capturas). Capturas usam dados fictícios. A validação de atributos e foco é automatizada; o anúncio por leitor de tela não foi verificado manualmente.

## Ambiente preparado nesta máquina

O checkout está em `local/c14-ticket-system` da pasta de trabalho. Na raiz dessa pasta de trabalho, `.local-runtime` contém PostgreSQL portátil, dados descartáveis, cache Maven e logs. Não versionar essa pasta nem o arquivo de senha. Para iniciar novamente a instância preparada, execute da raiz:

```powershell
& ./.local-runtime/postgresql/pgsql/bin/pg_ctl.exe -D ./.local-runtime/pgdata -l ./.local-runtime/postgres.log -o '-h 127.0.0.1 -p 55432' -w start
$env:C14_E2E_DB_PASSWORD=[IO.File]::ReadAllText((Resolve-Path ./.local-runtime/e2e-db-password.txt))
```

Execute Maven dentro do checkout. Use `JAVA_HOME` e o caminho do executável Maven caso não estejam no PATH. Ao terminar, encerre somente esta instância:

```powershell
& ./.local-runtime/postgresql/pgsql/bin/pg_ctl.exe -D ./.local-runtime/pgdata -m fast -w stop
```
