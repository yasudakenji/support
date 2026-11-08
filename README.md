# Support

Sistema de manutenção preventiva por horímetro. O Spring Boot registra equipamentos, leituras e revisões no PostgreSQL e calcula os alertas. O Vue exibe os dados e oferece ações de acordo com o perfil autenticado.

## Pré-requisitos

Instale estas ferramentas antes de abrir os terminais do projeto.

| Ferramenta | Para que serve |
| --- | --- |
| Git | Baixa uma cópia do repositório. [Instalar Git](https://git-scm.com/install/) |
| JDK 25 | Compila e executa o backend Java. [Baixar JDK 25](https://www.oracle.com/java/technologies/downloads/#java25) |
| Docker com Compose | Inicia o PostgreSQL. No Windows, instale e abra o Docker Desktop. [Instalar Docker Desktop no Windows](https://docs.docker.com/desktop/setup/install/windows-install/) |
| Node.js 22.12 ou superior | Instala as dependências e executa o frontend Vue. O npm vem com o Node.js. [Baixar Node.js](https://nodejs.org/en/download) |
| PowerShell 7.2 ou superior, no Windows | Executa os comandos abaixo e permite digitar senhas sem exibi-las. [Instalar PowerShell 7](https://learn.microsoft.com/en-us/powershell/scripting/install/installing-powershell-on-windows) |

O Maven Wrapper vem no repositório e baixa o Maven na primeira execução. Com o Docker, o PostgreSQL roda no contêiner. Você pode seguir o guia só com os terminais e um navegador.

## Instalação no Windows

### 1. Confira as ferramentas e baixe o projeto

Abra o Docker Desktop e espere o Docker iniciar. Abra o **PowerShell 7** e verifique as versões.

```powershell
git --version
java -version
javac -version
docker compose version
node --version
npm.cmd --version
```

O `java` e o `javac` devem indicar a versão 25. Se algum comando não for reconhecido após a instalação, feche e reabra o terminal e tente de novo.

Na página do repositório no GitHub, clique em **Code → HTTPS** e copie a URL. No PowerShell, entre na pasta onde quer guardar o projeto. Execute os comandos abaixo. A URL é a mesma exibida no botão **Code**. Se o repositório for privado, use uma conta GitHub que tenha acesso a ele.

```powershell
git clone https://github.com/yasudakenji/support.git support
cd support
```

A pasta `support` é a raiz do projeto. Nela ficam `pom.xml`, `compose.yaml` e a pasta `frontend`.

### 2. Terminal 1 · Configure as senhas e inicie o banco

Na raiz do projeto, execute as três linhas. Escolha uma senha para cada pergunta e pressione **Enter**. Os caracteres ficam ocultos enquanto você digita.

```powershell
$env:SUPPORT_DB_PASSWORD = Read-Host -Prompt "Senha do PostgreSQL" -MaskInput
$env:SUPPORT_ADMIN_PASSWORD = Read-Host -Prompt "Senha da conta admin" -MaskInput
$env:SUPPORT_USER_PASSWORD = Read-Host -Prompt "Senha da conta usuario" -MaskInput
```

A primeira senha é usada pelo backend para acessar o banco. As outras duas são usadas para entrar no Support. Para `admin` e `usuario`, escolha senhas distintas de 12 a 72 caracteres ASCII, como letras sem acento, números e símbolos. As variáveis ficam disponíveis no terminal em que você as definiu.

No mesmo terminal, inicie o PostgreSQL e confira seu estado.

```powershell
docker compose up -d --wait postgres
docker compose ps postgres
```

Espere o comando terminar e confira se o contêiner aparece como `healthy`. O Compose disponibiliza o banco em **127.0.0.1:5433** e aplica `banco/schema.sql` na primeira inicialização de um volume vazio. Os equipamentos e planos começam vazios.

### 3. Terminal 1 · Inicie o backend

No mesmo terminal, ainda na raiz do projeto, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

A primeira execução pode levar alguns minutos para baixar as dependências. Quando aparecer `Started SupportApplication`, a API estará rodando na porta **8082**. Mantenha o terminal aberto.

### 4. Terminal 2 · Inicie o frontend

Abra outro PowerShell 7 e entre na pasta `support` que você clonou no passo 1. A partir dela, execute:

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

O primeiro comando entra na pasta do Vue, o segundo instala as dependências e o terceiro inicia o servidor do frontend. Deixe esse terminal aberto e use o endereço indicado na linha **Local** do Vite, normalmente [http://127.0.0.1:5173](http://127.0.0.1:5173/).

### 5. Faça o primeiro login

Na tela **Entrar no Support**, use um dos logins abaixo com a senha que você escolheu no Terminal 1.

| Usuário | Senha definida em | Permissão |
| --- | --- | --- |
| `admin` | `SUPPORT_ADMIN_PASSWORD` | Consultar, cadastrar, editar e excluir |
| `usuario` | `SUPPORT_USER_PASSWORD` | Somente consulta |

Para começar com a conta `admin`, abra **Gerenciar setores**, crie um setor e depois use **Novo equipamento** para cadastrar o primeiro item. Você pode seguir o [fluxo de demonstração](#fluxo-de-demonstração) para testar os alertas.

### Encerrar e abrir de novo

Use **Ctrl + C** no terminal do Vue e no terminal do Spring. Para parar o banco, execute `docker compose stop postgres` na raiz, no terminal com a senha do banco definida. Os dados ficam no volume do PostgreSQL.

Quando quiser voltar, abra o Docker Desktop e repita os passos 2 a 4. Se o terminal foi fechado, informe as senhas novamente e use a mesma senha do banco que escolheu na primeira instalação. O `npm ci` pode ser pulado se as dependências já estiverem instaladas e o arquivo `package-lock.json` estiver igual.

## Instalação no Linux ou macOS

No Linux, inicie o Docker Engine com o plugin Compose. No macOS, abra o Docker Desktop. No Bash, clone o repositório e entre na pasta criada. Use uma conta GitHub com acesso ao repositório.

```bash
git clone https://github.com/yasudakenji/support.git support
cd support
```

No **Terminal 1**, confira `java -version`, `javac -version`, `docker compose version` e `node --version`. Defina as três senhas, inicie o PostgreSQL e depois o Spring Boot.

```bash
read -rsp 'Senha do PostgreSQL: ' SUPPORT_DB_PASSWORD; printf '\n'
read -rsp 'Senha da conta admin: ' SUPPORT_ADMIN_PASSWORD; printf '\n'
read -rsp 'Senha da conta usuario: ' SUPPORT_USER_PASSWORD; printf '\n'
export SUPPORT_DB_PASSWORD SUPPORT_ADMIN_PASSWORD SUPPORT_USER_PASSWORD

docker compose up -d --wait postgres
./mvnw spring-boot:run
```

Espere aparecer `Started SupportApplication`. No **Terminal 2**, a partir da raiz do projeto, execute:

```bash
cd frontend
npm ci
npm run dev
```

Abra o endereço **Local** do Vite e use as contas descritas em [Faça o primeiro login](#5-faça-o-primeiro-login).

## Solução de problemas

| O que apareceu | O que conferir |
| --- | --- |
| O Docker não conseguiu conectar ao daemon | No Windows ou no macOS, abra o Docker Desktop e espere ele iniciar. No Linux, confirme que o Docker Engine está em execução. |
| A porta `5433` está ocupada | Use uma porta livre para o banco. No Terminal 1, defina `$env:SUPPORT_DB_PORT = '55433'` no PowerShell ou `export SUPPORT_DB_PORT=55433` no Bash. Nesse mesmo terminal, rode novamente o Compose e o Spring. |
| O Spring falhou ao conectar ao PostgreSQL | Confira se o contêiner está `healthy` e se `SUPPORT_DB_PASSWORD` foi definida no terminal que executa o Maven. Para um volume existente, use a senha configurada na primeira inicialização. |
| A tela diz que o servidor não concluiu a operação | Confira se o Terminal 1 mostra `Started SupportApplication` e continua aberto. O Vue encaminha as chamadas para a API em `127.0.0.1:8082`. |
| Usuário ou senha inválidos | Para a conta de consulta, digite `usuario`, sem acento. Use a senha da conta correspondente, escolhida antes de iniciar o Spring. Se alterou as variáveis, reinicie o backend no mesmo terminal. |

### Usar um PostgreSQL instalado separadamente

Crie um banco vazio chamado `support` e aplique o esquema do projeto a partir da raiz. Use uma instalação local e ajuste a porta e o usuário conforme necessário.

```text
psql -h localhost -p 5433 -U postgres -d support -v ON_ERROR_STOP=1 -f banco/schema.sql
```

Defina `SUPPORT_DB_PASSWORD` e, se precisar, `SUPPORT_DB_PORT`, `SUPPORT_DB_USER` e `SUPPORT_DB_NAME`. Os padrões são `5433`, `postgres` e `support`. Siga a partir da inicialização do backend. Em bancos existentes, alterações de estrutura exigem um script de migração próprio.


## Fluxo de demonstração

1. Entre como administrador e cadastre um setor e um equipamento com 590 h.
2. Crie um plano **Troca de óleo** a cada 500 h. Sem revisão no histórico, o cartão do plano mostra **Sem revisão registrada**. No painel de alertas, o plano aparece como **Avaliação inicial pendente**.
3. Registre uma revisão anterior em 100 h. A próxima revisão passa a ser em 600 h. Como faltam 10 h, o plano fica **Próxima**.
4. Registre uma nova leitura de 600 h. Ela aparece no histórico, atualiza o horímetro e muda o alerta para **Vencida**. Use o filtro no painel de alertas para encontrá-la.
5. Registre a revisão em 600 h e confira o histórico. A próxima troca passa a ser em 1.100 h e o plano sai da lista de vencidas.

O backend classifica um plano como próximo quando faltam até 20 h positivas e como vencido ao atingir ou passar do limite. A referência de cada plano é a última revisão registrada. O sistema registra manutenções executadas.

## Contas de demonstração

| Usuário | Variável da senha | Acesso |
|---|---|---|
| admin | SUPPORT_ADMIN_PASSWORD | Consulta, cadastro, edição e exclusão |
| usuario | SUPPORT_USER_PASSWORD | Consulta de equipamentos, setores, planos, revisões, leituras e alertas |

O Spring Security cria as contas de demonstração em memória na inicialização e guarda o hash BCrypt. Cada requisição autentica via **HTTP Basic**. A interface guarda a credencial somente em memória e pede login ao recarregar a página. Se a senha de uma conta de demonstração não for definida, essa conta não é criada. Em uma execução pelo IntelliJ, coloque as mesmas variáveis na configuração de execução. Use credenciais de demonstração e mantenha o HTTP Basic local. Em um ambiente remoto, use HTTPS.

### Cadastrar uma conta no banco

Entre como administrador e abra o menu de conta no canto superior direito. Selecione **Cadastrar usuário**, informe nome, e-mail e senha, e escolha **Somente consulta** ou **Administrador**. A nova conta entra com o e-mail e a senha informados. O PostgreSQL guarda o ID, o nome, o e-mail, o hash BCrypt e o perfil. O cadastro também está disponível para administradores por **POST /usuario/salvar**.

### Testar pela API

- Configure **Basic Auth** no Postman. Com qualquer uma das contas, teste **GET /equipamento/listar**.
- Para gravar como administrador, primeiro faça **GET /conta/atual** com Basic Auth. Mantenha no Postman o cookie **XSRF-TOKEN** recebido e envie no cabeçalho **X-XSRF-TOKEN** o valor de **csrfToken** do JSON nas requisições **POST**, **PUT** e **DELETE**. Faça as chamadas no mesmo host.
- A conta **usuario** pode consultar, mas recebe **403** ao tentar gravar. Consultas sem autenticação recebem **401**.

## Testes

Com o PostgreSQL ligado e as variáveis do banco no terminal, execute `.\mvnw.cmd test` no Windows ou `./mvnw test` no Linux e macOS. Os testes de integração usam o esquema do Support e revertem seus dados temporários.
