<script setup>
import { computed, ref } from 'vue'
import { api } from './api'
import { agoraLocal, data, dataHora, horas } from './formatacao'
import { mensagemRevisao, niveisVisuaisRevisao } from './revisao'
import JanelaFormulario from './components/JanelaFormulario.vue'
import SeletorDataHora from './components/SeletorDataHora.vue'
import FormularioLogin from './components/FormularioLogin.vue'
import FormularioPlano from './components/FormularioPlano.vue'
import FormularioUsuario from './components/FormularioUsuario.vue'
import PlanoCard from './components/PlanoCard.vue'
import PainelAlertas from './components/PainelAlertas.vue'

const equipamentos = ref([])
const setores = ref([])
const filtroSetor = ref('')
const busca = ref('')
const paginaLista = ref('equipamentos')
const equipamentoId = ref(null)
const equipamento = ref(null)
const planos = ref([])
const revisoes = ref([])
const filtroPlanoRevisoes = ref('')
const leituras = ref([])
const aba = ref('planos')
const carregando = ref(false)
const erro = ref('')
const aviso = ref('')
const formulario = ref(null)
const dados = ref({})
const salvando = ref(false)
const erroFormulario = ref('')
const menuConta = ref(null)
const conta = ref(null)
const podeAlterar = computed(() => conta.value?.administrador === true)
let consultaAtual = 0

const abas = [
  { id: 'planos', nome: 'Planos de manutenção' },
  { id: 'revisoes', nome: 'Revisões realizadas' },
  { id: 'leituras', nome: 'Leituras do horímetro' },
]
const nomesPlanos = computed(() => Object.fromEntries(
  planos.value.map(plano => [plano.id, plano.nomePlano])
))
const revisoesFiltradas = computed(() => filtroPlanoRevisoes.value
  ? revisoes.value.filter(item => item.planoManutencaoId === filtroPlanoRevisoes.value)
  : revisoes.value
)
const nomesSetores = computed(() => new Map(setores.value.map(setor => [setor.id, setor.nome])))
const normalizarBusca = texto => texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase()
const equipamentosFiltrados = computed(() => {
  const termo = normalizarBusca(busca.value.trim())
  return equipamentos.value.filter(item =>
    normalizarBusca(item.nome).includes(termo) || normalizarBusca(item.modelo).includes(termo)
  )
})
const alertasPorPlano = computed(() => {
  const equipamentosPorId = new Map(equipamentos.value.map(item => [item.id, item]))
  const resultado = []

  for (const plano of planos.value) {
    const item = equipamentosPorId.get(plano.equipamentoId)
    if (!item || plano.statusRevisao === 'EM_DIA') continue

    resultado.push({ plano, equipamento: item, nivel: niveisVisuaisRevisao[plano.statusRevisao] })
  }
  return resultado
})

const alertas = computed(() => {
  const resultado = new Map()

  for (const { plano, equipamento: item, nivel } of alertasPorPlano.value) {
    if (plano.statusRevisao === 'AVALIACAO_INICIAL_PENDENTE') continue

    const atual = resultado.get(item.id)
    const restante = plano.horasRestantes
    if (!atual || restante < atual.restante) {
      resultado.set(item.id, {
        nivel, restante,
        mensagem: plano.nomePlano + '. ' + mensagemRevisao(restante),
      })
    }
  }
  return resultado
})

function fecharMenuConta(evento) {
  const menu = menuConta.value
  if (!menu) return
  if (evento.type === 'focusout' && menu.contains(evento.relatedTarget)) return
  menu.open = false
  if (evento.type === 'keydown') menu.querySelector('summary').focus()
}

function abrirConta(contaAutenticada) {
  conta.value = contaAutenticada
  navegar()
}

function abrirCadastroUsuario() {
  menuConta.value.open = false
  abrirFormulario('novoUsuario')
}

function sair() {
  api.sair()
  conta.value = null
  setores.value = []
  filtroSetor.value = ''
  busca.value = ''
  erro.value = ''
  carregando.value = false
  fecharFormulario()
  navegar()
}

async function carregar() {
  const consulta = ++consultaAtual
  if (!conta.value) return
  const id = equipamentoId.value
  carregando.value = true
  erro.value = ''
  try {
    if (paginaLista.value === 'setores') {
      const listaSetores = await api.listarSetores()
      if (consulta === consultaAtual) setores.value = listaSetores
      return
    }

    if (!id) {
      const [lista, listaSetores] = await Promise.all([
        api.listarEquipamentos(paginaLista.value === 'alertas' ? undefined : filtroSetor.value),
        api.listarSetores(),
      ])
      if (consulta !== consultaAtual) return
      setores.value = listaSetores
      equipamentos.value = lista
      planos.value = []
      revisoes.value = []
      if (!lista.length) return

      const todosPlanos = await api.listarPlanos()
      if (consulta !== consultaAtual) return
      planos.value = todosPlanos
      return
    }

    const [item, planosDoEquipamento, leiturasDoEquipamento] = await Promise.all([
      api.buscarEquipamento(id),
      api.listarPlanos(id),
      api.listarLeituras(id),
    ])
    const revisoesPorPlano = await Promise.all(
      planosDoEquipamento.map(plano => api.listarRevisoes(plano.id))
    )
    if (consulta !== consultaAtual) return
    equipamento.value = item
    planos.value = planosDoEquipamento
    if (filtroPlanoRevisoes.value && !planosDoEquipamento.some(plano => plano.id === filtroPlanoRevisoes.value)) {
      filtroPlanoRevisoes.value = ''
    }
    leituras.value = leiturasDoEquipamento
    revisoes.value = revisoesPorPlano.flat().sort((a, b) =>
      b.dataManutencao.localeCompare(a.dataManutencao) ||
      b.horimetroNaExecucao - a.horimetroNaExecucao
    )
  } catch (falha) {
    if (consulta === consultaAtual) erro.value = falha.message
  } finally {
    if (consulta === consultaAtual) carregando.value = false
  }
}

function abrirSetor(id) {
  filtroSetor.value = id
  busca.value = ''
  navegar()
}

function navegar(id = null, pagina = 'equipamentos') {
  paginaLista.value = pagina
  equipamentoId.value = id
  equipamento.value = null
  equipamentos.value = []
  planos.value = []
  revisoes.value = []
  leituras.value = []
  aba.value = 'planos'
  filtroPlanoRevisoes.value = ''
  aviso.value = ''
  window.scrollTo(0, 0)
  carregar()
}

function abrirFormulario(tipo, item) {
  if (!podeAlterar.value) return
  erroFormulario.value = ''
  aviso.value = ''
  const agora = agoraLocal()
  const horimetroAtual = Number(equipamento.value?.horimetroAtual ?? 0)

  switch (tipo) {
    case 'novoEquipamento':
      dados.value = { nome: '', modelo: '', horimetroAtual: 0, setorId: filtroSetor.value || null }
      formulario.value = { tipo, titulo: 'Novo equipamento', confirmar: 'Cadastrar equipamento' }
      break
    case 'editarEquipamento':
      dados.value = { ...equipamento.value }
      formulario.value = { tipo, titulo: 'Editar equipamento', confirmar: 'Salvar' }
      break
    case 'excluirEquipamento':
      dados.value = { id: equipamentoId.value }
      formulario.value = { tipo, titulo: 'Excluir equipamento', confirmar: 'Excluir equipamento' }
      break
    case 'novoSetor':
      dados.value = { nome: '' }
      formulario.value = { tipo, titulo: 'Novo setor', confirmar: 'Cadastrar setor' }
      break
    case 'editarSetor':
      dados.value = { ...item }
      formulario.value = { tipo, titulo: 'Editar setor', confirmar: 'Salvar' }
      break
    case 'excluirSetor':
      dados.value = { ...item }
      formulario.value = { tipo, titulo: 'Excluir setor', confirmar: 'Excluir setor' }
      break
    case 'novoPlano':
    case 'novoUsuario':
      formulario.value = { tipo }
      break
    case 'revisao':
      dados.value = {
        planoManutencaoId: item.id, dataManutencao: agora.slice(0, 10),
        horimetroNaExecucao: horimetroAtual, descricaoExecucao: '',
      }
      formulario.value = { tipo, titulo: 'Registrar revisão · ' + item.nomePlano, confirmar: 'Registrar revisão' }
      break
    case 'excluirRevisao':
      dados.value = { ...item }
      formulario.value = { tipo, titulo: 'Excluir revisão', confirmar: 'Excluir revisão' }
      break
    case 'excluirLeitura':
      dados.value = { ...item }
      formulario.value = { tipo, titulo: 'Excluir leitura', confirmar: 'Excluir leitura' }
      break
    case 'leitura':
      dados.value = { equipamentoId: equipamentoId.value, dataHoraLeitura: agora, valorHorimetro: horimetroAtual }
      formulario.value = { tipo, titulo: 'Registrar leitura do horímetro', confirmar: 'Registrar leitura' }
      break
  }
}

function fecharFormulario() {
  formulario.value = null
  erroFormulario.value = ''
}

async function salvarFormulario(dadosDoFormulario) {
  if (salvando.value) return
  salvando.value = true
  erroFormulario.value = ''
  try {
    switch (formulario.value.tipo) {
      case 'novoEquipamento':
        await api.salvarEquipamento(dados.value)
        aviso.value = 'Equipamento cadastrado.'
        break
      case 'editarEquipamento':
        await api.atualizarEquipamento(dados.value)
        aviso.value = 'Equipamento atualizado.'
        break
      case 'excluirEquipamento':
        await api.excluirEquipamento(dados.value.id)
        equipamentoId.value = null
        equipamento.value = null
        aviso.value = 'Equipamento e registros vinculados excluídos.'
        break
      case 'novoSetor':
        await api.salvarSetor(dados.value)
        aviso.value = 'Setor cadastrado.'
        break
      case 'editarSetor':
        await api.atualizarSetor(dados.value)
        aviso.value = 'Setor atualizado.'
        break
      case 'excluirSetor':
        await api.excluirSetor(dados.value.id)
        if (filtroSetor.value === dados.value.id) filtroSetor.value = ''
        aviso.value = 'Setor e registros vinculados excluídos.'
        break
      case 'novoPlano':
        await api.salvarPlano({ ...dadosDoFormulario, equipamentoId: equipamentoId.value })
        aba.value = 'planos'
        aviso.value = 'Plano cadastrado.'
        break
      case 'novoUsuario':
        await api.salvarUsuario(dadosDoFormulario)
        aviso.value = 'Usuário cadastrado. A nova conta pode entrar com o e-mail informado.'
        break
      case 'revisao':
        await api.registrarRevisao({
          ...dados.value,
          descricaoExecucao: dados.value.descricaoExecucao.trim() || null,
        })
        aviso.value = 'Revisão registrada.'
        break
      case 'excluirRevisao':
        await api.excluirRevisao(dados.value.id)
        aviso.value = 'Revisão excluída.'
        break
      case 'excluirLeitura':
        await api.excluirLeitura(dados.value.id)
        aviso.value = 'Leitura excluída do histórico.'
        break
      case 'leitura':
        if (dados.value.dataHoraLeitura > agoraLocal()) {
          erroFormulario.value = 'A data e a hora da leitura não podem estar no futuro. Informe o horário atual ou um anterior.'
          return
        }
        await api.registrarLeitura(dados.value)
        aviso.value = 'Leitura registrada. Horímetro atualizado.'
        break
    }
    fecharFormulario()
    await carregar()
  } catch (falha) {
    erroFormulario.value = falha.message
  } finally {
    salvando.value = false
  }
}

function planoAlterado(mensagem) {
  aviso.value = mensagem
  carregar()
}

</script>

<template>
  <header class="barra">
    <div class="barra-conteudo">
      <button class="marca" type="button" @click="navegar()">SUPPORT</button>
      <span class="barra-descricao">Manutenção preventiva</span>
      <nav v-if="conta" class="menu-principal" aria-label="Menu principal">
        <button type="button" :aria-current="paginaLista === 'equipamentos' ? 'page' : undefined" @click="navegar()">Equipamentos</button>
        <button type="button" :aria-current="paginaLista === 'alertas' ? 'page' : undefined" @click="navegar(null, 'alertas')">Alertas</button>
      </nav>
      <details v-if="conta" ref="menuConta" class="menu-conta" @focusout="fecharMenuConta" @keydown.esc.stop="fecharMenuConta">
        <summary class="avatar" aria-label="Menu da conta">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true">
            <circle cx="12" cy="8" r="3.2"/>
            <path d="M5.5 20v-2a4 4 0 0 1 4-4h5a4 4 0 0 1 4 4v2"/>
          </svg>
        </summary>
        <div class="opcoes-conta">
          <p>{{ conta.nome || conta.usuario }} · {{ podeAlterar ? 'Administrador' : 'Somente consulta' }}</p>
          <button v-if="podeAlterar" type="button" @click="abrirCadastroUsuario">Cadastrar usuário</button>
          <button type="button" @click="sair">Trocar de conta</button>
          <button type="button" @click="sair">Sair</button>
        </div>
      </details>
    </div>
  </header>
  <main class="pagina" :class="{ 'pagina-detalhe': equipamentoId }" :aria-busy="carregando">
    <p v-if="aviso" class="aviso" role="status">{{ aviso }}</p>
    <div v-if="erro" class="painel-erro" role="alert">
      <p>{{ erro }}</p>
      <button type="button" :disabled="carregando" @click="carregar">Tentar novamente</button>
    </div>

    <FormularioLogin v-if="!conta" @autenticado="abrirConta" />
    <template v-else-if="!equipamentoId && paginaLista === 'setores'">
      <button type="button" class="voltar" @click="navegar()">← Equipamentos</button>
      <div class="cabecalho cabecalho-inicio divisoria-inferior">
        <div><h1>Setores</h1><p class="texto-secundario">Organize os equipamentos por local de trabalho.</p></div>
        <button v-if="podeAlterar" type="button" class="principal" :disabled="carregando" @click="abrirFormulario('novoSetor')">+ Novo setor</button>
      </div>
      <p v-if="carregando && !setores.length" class="estado">Carregando setores…</p>
      <p v-else-if="!erro && !setores.length" class="superficie estado">Nenhum setor cadastrado.</p>
      <section v-if="setores.length" class="grade" aria-label="Setores cadastrados">
        <article v-for="setor in setores" :key="setor.id" class="superficie cartao-setor">
          <h2>{{ setor.nome }}</h2>
          <p class="texto-secundario">{{ setor.totalEquipamentos }} {{ setor.totalEquipamentos === 1 ? 'equipamento cadastrado' : 'equipamentos cadastrados' }}</p>
          <div class="acoes">
            <button type="button" :aria-label="'Ver equipamentos de ' + setor.nome" @click="abrirSetor(setor.id)">Ver equipamentos</button>
            <template v-if="podeAlterar">
              <button type="button" @click="abrirFormulario('editarSetor', setor)">Editar</button>
              <button type="button" class="perigo" @click="abrirFormulario('excluirSetor', setor)">Excluir</button>
            </template>
          </div>
        </article>
      </section>
    </template>

    <PainelAlertas v-else-if="!equipamentoId && paginaLista === 'alertas'"
      :alertas="alertasPorPlano" :nomes-setores="nomesSetores" :carregando="carregando" :erro="erro"
      @abrir-equipamento="navegar($event, 'alertas')" />

    <template v-else-if="!equipamentoId">
      <div class="cabecalho cabecalho-inicio">
        <div>
          <p class="contexto">Projeto Support</p>
          <h1>{{ nomesSetores.has(filtroSetor) ? 'Equipamentos · ' + nomesSetores.get(filtroSetor) : 'Equipamentos' }}</h1>
          <p class="texto-secundario">Consulte o horímetro, os planos e as revisões de cada equipamento.</p>
        </div>
      </div>
      <div class="filtros-equipamentos divisoria-inferior">
        <label class="filtro-setor">Filtrar por setor
          <select v-model="filtroSetor" @change="carregar">
            <option value="">Todos os setores</option>
            <option v-for="setor in setores" :key="setor.id" :value="setor.id">{{ setor.nome }}</option>
          </select>
        </label>
        <label class="busca-equipamento">Buscar equipamento
          <input v-model="busca" type="search" placeholder="Nome ou modelo">
        </label>
        <div class="acoes acoes-equipamentos">
          <button v-if="podeAlterar" type="button" class="principal" :disabled="carregando" @click="abrirFormulario('novoEquipamento')">+ Novo equipamento</button>
          <button type="button" @click="navegar(null, 'setores')">{{ podeAlterar ? 'Gerenciar setores' : 'Ver setores' }}</button>
        </div>
      </div>
      <p v-if="carregando && !equipamentos.length" class="estado">Carregando equipamentos…</p>
      <div v-else-if="!erro && !equipamentosFiltrados.length" class="superficie estado">
        <template v-if="busca.trim()">
          <h2>Nenhum equipamento encontrado</h2>
          <p>Tente outro nome ou modelo.</p>
        </template>
        <template v-else>
          <h2>{{ filtroSetor ? 'Nenhum equipamento neste setor' : 'Nenhum equipamento cadastrado' }}</h2>
          <p>{{ filtroSetor ? 'Cadastre ou vincule um equipamento a este setor.' : 'Cadastre um equipamento para adicionar os primeiros planos de manutenção.' }}</p>
        </template>
      </div>
      <section v-if="equipamentosFiltrados.length" class="grade grade-equipamentos" aria-label="Equipamentos cadastrados">
        <button v-for="item in equipamentosFiltrados" :key="item.id" class="superficie cartao-equipamento" type="button" @click="navegar(item.id)">
          <span class="cartao-topo">
            <span class="contexto">{{ item.modelo }}</span>
            <span v-if="alertas.has(item.id)" class="alerta-cartao" :class="alertas.get(item.id).nivel"
              role="img" :aria-label="alertas.get(item.id).mensagem" :title="alertas.get(item.id).mensagem">
              <svg viewBox="0 0 24 24" width="23" height="23" aria-hidden="true">
                <path d="M10.3 4.1a2 2 0 0 1 3.4 0l8 13.8a2 2 0 0 1-1.7 3H4a2 2 0 0 1-1.7-3z" fill="currentColor"/>
                <path d="M12 9v4.7" class="sinal-alerta" stroke-width="1.9" stroke-linecap="round"/>
                <circle cx="12" cy="17.2" r="1" class="ponto-alerta"/>
              </svg>
            </span>
          </span>
          <strong :title="item.nome">{{ item.nome }}</strong>
          <span class="texto-secundario">{{ nomesSetores.get(item.setorId) || 'Sem setor' }}</span>
          <span class="horimetro">{{ horas(item.horimetroAtual) }}</span>
          <span class="texto-secundario">Horímetro atual</span>
          <span class="abrir-cartao">Ver equipamento →</span>
        </button>
      </section>
    </template>

    <template v-else>
      <button type="button" class="voltar" @click="navegar(null, paginaLista)">
        {{ paginaLista === 'alertas' ? '← Alertas' : '← Equipamentos' }}
      </button>
      <p v-if="carregando && !equipamento" class="estado">Carregando equipamento…</p>
      <template v-if="equipamento">
        <div class="cabecalho">
          <div><h1>{{ equipamento.nome }}</h1><p class="texto-secundario">Dados do equipamento e manutenção preventiva</p></div>
          <div v-if="podeAlterar" class="acoes">
            <button type="button" @click="abrirFormulario('editarEquipamento')">Editar equipamento</button>
            <button type="button" class="principal" @click="abrirFormulario('leitura')">+ Registrar leitura</button>
          </div>
        </div>
        <section class="superficie resumo" aria-label="Dados do equipamento">
          <dl class="dados-equipamento">
            <div><dt>Nome</dt><dd>{{ equipamento.nome }}</dd></div>
            <div><dt>Modelo</dt><dd>{{ equipamento.modelo }}</dd></div>
            <div><dt>Setor</dt><dd>{{ nomesSetores.get(equipamento.setorId) || 'Sem setor' }}</dd></div>
            <div><dt>Data de cadastro</dt><dd>{{ data(equipamento.dataCadastro) }}</dd></div>
          </dl>
          <div class="resumo-horimetro">
            <span class="texto-secundario">Horímetro atual</span>
            <strong class="horimetro">{{ horas(equipamento.horimetroAtual) }}</strong>
            <span v-if="leituras.length" class="texto-secundario">Última leitura em {{ dataHora(leituras[0].dataHoraLeitura) }}</span>
          </div>
        </section>

        <nav class="abas" aria-label="Informações do equipamento">
          <button v-for="item in abas" :key="item.id" type="button" :aria-pressed="aba === item.id" @click="aba = item.id">{{ item.nome }}</button>
        </nav>

        <section v-if="aba === 'planos'" class="secao">
          <div class="cabecalho">
            <div><h2>Planos deste equipamento</h2><p class="texto-secundario">Cada ação tem seu intervalo e seu histórico.</p></div>
            <button v-if="podeAlterar" type="button" @click="abrirFormulario('novoPlano')">+ Novo plano</button>
          </div>
          <p v-if="!planos.length" class="superficie estado">Nenhum plano cadastrado para este equipamento.</p>
          <PlanoCard v-for="plano in planos" :key="plano.id"
            :plano="plano" :pode-alterar="podeAlterar"
            @registrar-revisao="abrirFormulario('revisao', $event)" @alterado="planoAlterado" />
        </section>
        <section v-else-if="aba === 'revisoes'" class="secao">
          <div class="cabecalho">
            <h2>Revisões realizadas</h2>
            <label v-if="planos.length" class="filtro-plano">Filtrar por plano
              <select v-model="filtroPlanoRevisoes">
                <option value="">Todos os planos</option>
                <option v-for="plano in planos" :key="plano.id" :value="plano.id">{{ plano.nomePlano }}</option>
              </select>
            </label>
          </div>
          <p v-if="!revisoesFiltradas.length" class="superficie estado">
            {{ filtroPlanoRevisoes ? 'Nenhuma revisão registrada para este plano.' : 'Nenhuma revisão registrada.' }}
          </p>
          <div v-else class="superficie tabela">
            <table>
              <thead><tr><th scope="col">Data</th><th scope="col">Plano</th><th scope="col">Horímetro na execução</th><th scope="col">Descrição da execução</th><th v-if="podeAlterar" scope="col">Ações</th></tr></thead>
              <tbody>
                <tr v-for="item in revisoesFiltradas" :key="item.id">
                  <td>{{ data(item.dataManutencao) }}</td><td>{{ nomesPlanos[item.planoManutencaoId] }}</td>
                  <td>{{ horas(item.horimetroNaExecucao) }}</td><td class="texto-execucao">{{ item.descricaoExecucao || 'Não informada' }}</td>
                  <td v-if="podeAlterar"><button type="button" class="perigo" @click="abrirFormulario('excluirRevisao', item)">Excluir</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
        <section v-else class="secao">
          <h2>Leituras do horímetro</h2>
          <p v-if="!leituras.length" class="superficie estado">Nenhuma leitura registrada.</p>
          <div v-else class="superficie tabela">
            <table>
              <thead><tr><th scope="col">Data e hora</th><th scope="col">Horímetro</th><th scope="col">Uso desde a leitura anterior</th><th v-if="podeAlterar" scope="col">Ações</th></tr></thead>
              <tbody>
                <tr v-for="(item, indice) in leituras" :key="item.id">
                  <td>{{ dataHora(item.dataHoraLeitura) }}</td><td>{{ horas(item.valorHorimetro) }}</td>
                  <td>{{ leituras[indice + 1] ? horas(item.valorHorimetro - leituras[indice + 1].valorHorimetro) : '—' }}</td>
                  <td v-if="podeAlterar"><button type="button" class="perigo" @click="abrirFormulario('excluirLeitura', item)">Excluir</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
        <div v-if="podeAlterar" class="rodape-acoes">
          <button type="button" class="perigo" @click="abrirFormulario('excluirEquipamento')">Excluir equipamento</button>
        </div>
      </template>
    </template>
  </main>

  <FormularioPlano v-if="formulario?.tipo === 'novoPlano'" :ocupado="salvando" :erro="erroFormulario"
    @fechar="fecharFormulario" @salvar="salvarFormulario" />
  <FormularioUsuario v-else-if="formulario?.tipo === 'novoUsuario'" :ocupado="salvando" :erro="erroFormulario"
    @fechar="fecharFormulario" @salvar="salvarFormulario" />
  <JanelaFormulario v-else-if="formulario" :titulo="formulario.titulo" :confirmar="formulario.confirmar"
    :ocupado="salvando" :erro="erroFormulario" :perigo="formulario.tipo.startsWith('excluir')"
    @fechar="fecharFormulario" @salvar="salvarFormulario">
    <template v-if="formulario.tipo === 'novoEquipamento' || formulario.tipo === 'editarEquipamento'">
      <label>Nome<input v-model.trim="dados.nome" required maxlength="50" autofocus></label>
      <label>Modelo<input v-model.trim="dados.modelo" required maxlength="50"></label>
      <label class="largura-total">Setor
        <select v-model="dados.setorId">
          <option :value="null">Sem setor</option>
          <option v-for="setor in setores" :key="setor.id" :value="setor.id">{{ setor.nome }}</option>
        </select>
      </label>
      <label class="largura-total">{{ formulario.tipo === 'editarEquipamento' ? 'Correção do horímetro (h)' : 'Horímetro inicial (h)' }}
        <input v-model.number="dados.horimetroAtual" type="number" required min="0" max="99999999.99" step="0.01">
      </label>
    </template>
    <p v-else-if="formulario.tipo === 'excluirEquipamento'" class="largura-total">
      Excluir <strong>{{ equipamento?.nome }}</strong> e todos os seus planos, revisões e leituras?
    </p>
    <label v-else-if="formulario.tipo === 'novoSetor' || formulario.tipo === 'editarSetor'" class="largura-total">
      Nome do setor<input v-model.trim="dados.nome" required maxlength="50" autofocus>
    </label>
    <p v-else-if="formulario.tipo === 'excluirSetor'" class="largura-total">
      Excluir <strong>{{ dados.nome }}</strong>?
      <template v-if="dados.totalEquipamentos">
        A exclusão inclui os <strong>{{ dados.totalEquipamentos }}</strong> equipamentos do setor, com seus planos, revisões e leituras.
      </template>
    </p>
    <template v-else-if="formulario.tipo === 'revisao'">
      <label>Data da revisão<input v-model="dados.dataManutencao" type="date" required :max="agoraLocal().slice(0, 10)" autofocus></label>
      <label>Horímetro na execução (h)<input v-model.number="dados.horimetroNaExecucao" type="number" required min="0" :max="equipamento.horimetroAtual" step="0.01"></label>
      <label class="largura-total">Descrição da execução (opcional)
        <textarea v-model="dados.descricaoExecucao" maxlength="1000" rows="3" placeholder="Peça usada, procedimento executado ou observações." />
        <small>Até 1.000 caracteres.</small>
      </label>
    </template>
    <p v-else-if="formulario.tipo === 'excluirRevisao'" class="largura-total">
      Excluir a revisão de <strong>{{ nomesPlanos[dados.planoManutencaoId] }}</strong>,
      registrada em {{ data(dados.dataManutencao) }} com {{ horas(dados.horimetroNaExecucao) }}?
    </p>
    <p v-else-if="formulario.tipo === 'excluirLeitura'" class="largura-total">
      Excluir do histórico a leitura de <strong>{{ horas(dados.valorHorimetro) }}</strong> de {{ dataHora(dados.dataHoraLeitura) }}?
      O horímetro atual do equipamento será preservado.
    </p>
    <template v-else-if="formulario.tipo === 'leitura'">
      <label>Horímetro acumulado (h)<input v-model.number="dados.valorHorimetro" type="number" required :min="equipamento.horimetroAtual" max="99999999.99" step="0.01" autofocus></label>
      <SeletorDataHora v-model="dados.dataHoraLeitura" />
    </template>
  </JanelaFormulario>
</template>
