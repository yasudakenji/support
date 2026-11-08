let autorizacao
let csrfToken

async function requisitar(caminho, metodo = 'GET', dados, credencial = autorizacao) {
  const headers = { 'X-Requested-With': 'XMLHttpRequest' }
  if (credencial) headers.Authorization = credencial
  if (dados !== undefined) headers['Content-Type'] = 'application/json'
  if (metodo !== 'GET' && csrfToken) headers['X-XSRF-TOKEN'] = csrfToken

  let resposta
  try {
    resposta = await fetch('/api' + caminho, {
      method: metodo,
      headers,
      body: dados === undefined ? undefined : JSON.stringify(dados),
    })
  } catch {
    throw new Error('Não foi possível acessar a API. Confira se o Spring Boot está em execução.')
  }

  if (resposta.ok) {
    const texto = await resposta.text()
    return texto ? JSON.parse(texto) : null
  }

  const erro = await resposta.json().catch(() => ({}))
  if (erro.detail || erro.message) throw new Error(erro.detail || erro.message)
  if (resposta.status === 401) throw new Error('Usuário ou senha inválidos. Confira os dados e tente novamente.')
  if (resposta.status === 403) throw new Error('A conta não tem permissão para esta operação.')
  if (resposta.status === 400) throw new Error('Confira os campos e os valores informados.')
  if (resposta.status === 404) throw new Error('O registro não foi encontrado.')
  if (resposta.status === 409) throw new Error('A operação conflita com os registros existentes.')
  throw new Error('O servidor não concluiu a operação. Confira o Spring Boot na porta 8082.')
}

const parametro = (nome, valor) => '?' + new URLSearchParams({ [nome]: valor })

export const api = {
  async entrar(usuario, senha) {
    const bytes = new TextEncoder().encode(usuario + ':' + senha)
    const credencial = 'Basic ' + btoa(String.fromCharCode(...bytes))
    const { csrfToken: token, ...conta } = await requisitar('/conta/atual', 'GET', undefined, credencial)
    autorizacao = credencial
    csrfToken = token
    return conta
  },
  sair() {
    autorizacao = undefined
    csrfToken = undefined
  },
  salvarUsuario: dados => requisitar('/usuario/salvar', 'POST', dados),
  listarEquipamentos: setorId => requisitar('/equipamento/listar' + (setorId ? parametro('setorId', setorId) : '')),
  buscarEquipamento: id => requisitar('/equipamento/buscar' + parametro('id', id)),
  salvarEquipamento: dados => requisitar('/equipamento/salvar', 'POST', dados),
  atualizarEquipamento: dados => requisitar('/equipamento/atualizar', 'PUT', dados),
  excluirEquipamento: id => requisitar('/equipamento/deletar' + parametro('id', id), 'DELETE'),
  listarSetores: () => requisitar('/setor/listar'),
  salvarSetor: dados => requisitar('/setor/salvar', 'POST', dados),
  atualizarSetor: dados => requisitar('/setor/atualizar', 'PUT', dados),
  excluirSetor: id => requisitar('/setor/deletar' + parametro('id', id), 'DELETE'),
  listarPlanos: id => requisitar('/plano-manutencao/listar' + (id ? parametro('equipamentoId', id) : '')),
  salvarPlano: dados => requisitar('/plano-manutencao/salvar', 'POST', dados),
  atualizarPlano: dados => requisitar('/plano-manutencao/atualizar', 'PUT', dados),
  excluirPlano: id => requisitar('/plano-manutencao/deletar' + parametro('id', id), 'DELETE'),
  listarRevisoes: id => requisitar('/manutencao/listar' + (id ? parametro('planoManutencaoId', id) : '')),
  registrarRevisao: dados => requisitar('/manutencao/salvar', 'POST', dados),
  excluirRevisao: id => requisitar('/manutencao/deletar' + parametro('id', id), 'DELETE'),
  listarLeituras: id => requisitar('/leitura-horimetro/listar' + parametro('equipamentoId', id)),
  registrarLeitura: dados => requisitar('/leitura-horimetro/salvar', 'POST', dados),
  excluirLeitura: id => requisitar('/leitura-horimetro/deletar' + parametro('id', id), 'DELETE'),
}
