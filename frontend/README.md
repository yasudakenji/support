# Support frontend

Frontend em Vue 3 e Vite. A página inicial lista os equipamentos e cada cartão abre os planos, as revisões e as leituras daquele equipamento.

Com o PostgreSQL e o Spring Boot em execução na porta 8082, rode estes comandos na pasta `frontend`.

```powershell
npm.cmd ci
npm.cmd run dev
```

Abra a URL local exibida pelo Vite. As chamadas para `/api` são encaminhadas ao Spring Boot pelo proxy em `vite.config.js`. A variável `SUPPORT_API_URL` permite usar outro endereço para o backend.

- `src/App.vue` organiza a lista, os detalhes e os formulários.
- `src/components/FormularioLogin.vue` recebe as credenciais e avisa o App quando o login é confirmado.
- `src/components/PlanoCard.vue` contém a edição e a exclusão de cada plano.
- `src/components/JanelaFormulario.vue` reúne os formulários em janelas centralizadas.
- `src/api.js` reúne as chamadas para os endpoints do Spring.

O Spring calcula a próxima revisão, as horas restantes e o status retornado pela API. O Vue usa esses dados nos cartões e no painel de alertas. Sem um registro de revisão, a tela exibe “A definir”. Para configurar as contas e o banco, consulte o README da raiz.
