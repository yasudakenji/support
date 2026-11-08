<script setup>
import { computed, ref } from 'vue'
import { horas } from '../formatacao'
import { mensagemRevisao } from '../revisao'

const props = defineProps({
  alertas: { type: Array, required: true },
  nomesSetores: { type: Map, required: true },
  carregando: Boolean,
  erro: String,
})
const emit = defineEmits(['abrir-equipamento'])
const selecionado = ref(null)
const filtroSituacao = ref('')
const rotulos = {
  vencido: 'Vencida',
  proximo: 'Próxima',
  neutro: 'Avaliação inicial pendente',
}
const totais = computed(() => {
  const resultado = { vencido: 0, proximo: 0, neutro: 0 }
  for (const { nivel } of props.alertas) resultado[nivel]++
  return resultado
})
const ordenados = computed(() => [...props.alertas].sort((a, b) =>
  (a.plano.horasRestantes ?? Number.MAX_VALUE) - (b.plano.horasRestantes ?? Number.MAX_VALUE) ||
  a.equipamento.nome.localeCompare(b.equipamento.nome, 'pt-BR') ||
  a.plano.nomePlano.localeCompare(b.plano.nomePlano, 'pt-BR')
))
const visiveis = computed(() => filtroSituacao.value
  ? ordenados.value.filter(item => item.nivel === filtroSituacao.value)
  : ordenados.value
)
</script>

<template>
  <section class="secao" aria-labelledby="titulo-alertas">
    <div class="cabecalho cabecalho-inicio">
      <div>
        <p class="contexto">Projeto Support</p>
        <h1 id="titulo-alertas">Painel de alertas</h1>
        <p class="texto-secundario">Planos de manutenção que precisam de atenção.</p>
      </div>
    </div>
    <p v-if="carregando" class="estado">Carregando alertas…</p>
    <template v-else-if="!erro">
      <div class="acoes barra-alertas divisoria-inferior">
        <ul class="acoes totais-alertas" aria-label="Resumo dos alertas">
          <li class="selo vencido"><strong>{{ totais.vencido }}</strong> {{ totais.vencido === 1 ? 'vencida' : 'vencidas' }}</li>
          <li class="selo proximo"><strong>{{ totais.proximo }}</strong> {{ totais.proximo === 1 ? 'próxima' : 'próximas' }}</li>
          <li class="selo neutro"><strong>{{ totais.neutro }}</strong> {{ totais.neutro === 1 ? 'avaliação inicial pendente' : 'avaliações iniciais pendentes' }}</li>
        </ul>
        <label class="filtro-situacao">Filtrar por situação
          <select v-model="filtroSituacao" @change="selecionado = null">
            <option value="">Todos os alertas</option>
            <option value="vencido">Vencidas</option>
            <option value="proximo">Próximas</option>
            <option value="neutro">Avaliação inicial pendente</option>
          </select>
        </label>
      </div>
      <p v-if="!visiveis.length" class="superficie estado">
        {{ filtroSituacao ? 'Nenhum alerta nessa situação.' : 'Nenhum plano precisa de atenção no momento.' }}
      </p>
      <div v-else class="superficie tabela tabela-alertas">
        <table aria-label="Planos que precisam de atenção">
          <thead>
            <tr><th scope="col">Equipamento</th><th scope="col">Setor</th><th scope="col">Plano de manutenção</th><th scope="col">Situação</th><th scope="col">Ações</th></tr>
          </thead>
          <tbody>
            <template v-for="item in visiveis" :key="item.plano.id">
              <tr class="linha-alerta">
                <th scope="row">{{ item.equipamento.nome }}</th>
                <td class="setor-alerta">{{ nomesSetores.get(item.equipamento.setorId) || 'Sem setor' }}</td>
                <td>{{ item.plano.nomePlano }}</td>
                <td class="situacao-alerta">
                  <span class="selo" :class="item.nivel">{{ rotulos[item.nivel] }}</span>
                  <span class="texto-secundario">{{ mensagemRevisao(item.plano.horasRestantes) }}</span>
                </td>
                <td class="acao-alerta">
                  <button type="button" class="editar"
                    :aria-label="'Ver plano ' + item.plano.nomePlano + ' · ' + item.equipamento.nome"
                    :aria-expanded="selecionado === item.plano.id"
                    :aria-controls="selecionado === item.plano.id ? 'alerta-' + item.plano.id : undefined"
                    @click="selecionado = selecionado === item.plano.id ? null : item.plano.id">Ver plano</button>
                </td>
              </tr>
              <tr v-if="selecionado === item.plano.id" class="linha-detalhe-alerta">
                <td :id="'alerta-' + item.plano.id" colspan="5">
                  <div class="cabecalho">
                    <dl class="dados-equipamento">
                      <div><dt>Horímetro atual</dt><dd>{{ horas(item.plano.horimetroAtual) }}</dd></div>
                      <div><dt>Última revisão</dt><dd>{{ item.plano.horimetroUltimaRevisao === null ? 'Sem registro' : horas(item.plano.horimetroUltimaRevisao) }}</dd></div>
                      <div><dt>Próxima revisão</dt><dd>{{ item.plano.proximoHorimetro === null ? 'A definir' : horas(item.plano.proximoHorimetro) }}</dd></div>
                    </dl>
                    <button type="button" @click="emit('abrir-equipamento', item.equipamento.id)">Abrir equipamento</button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </template>
  </section>
</template>

<style scoped>
.barra-alertas { justify-content: space-between; }
.filtro-situacao { width: min(230px, 100%); }
.totais-alertas { list-style: none; margin: 0; padding-inline: 0; }
.tabela-alertas th[scope="row"] { background: var(--superficie); color: var(--texto); font-size: 14px; }
.tabela-alertas tr:last-child th[scope="row"] { border-bottom: 0; }
.tabela-alertas .situacao-alerta > span { display: block; width: fit-content; }
.situacao-alerta .selo { margin-bottom: 4px; }
.acao-alerta button { white-space: nowrap; }
.linha-detalhe-alerta td { background: var(--fundo); }

@media (max-width: 650px) {
  .tabela-alertas table, .tabela-alertas tbody { display: block; min-width: 0; }
  .tabela-alertas thead { position: absolute; width: 1px; height: 1px; clip-path: inset(50%); overflow: hidden; }
  .linha-alerta { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 4px 8px; padding: 12px; }
  .tabela-alertas tr + tr { border-top: 1px solid var(--borda); }
  .linha-alerta :is(th, td) { grid-column: 1 / -1; padding: 0; border: 0; overflow-wrap: anywhere; }
  .linha-alerta .setor-alerta { color: var(--suave); font-size: 13px; }
  .linha-alerta .situacao-alerta { grid-column: 1; }
  .linha-alerta .acao-alerta { grid-column: 2; align-self: center; }
  .linha-detalhe-alerta, .linha-detalhe-alerta td { display: block; }
  .linha-detalhe-alerta td { border: 0; }
}
</style>
