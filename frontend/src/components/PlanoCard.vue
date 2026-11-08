<script setup>
import { ref } from 'vue'
import { api } from '../api'
import { horas } from '../formatacao'
import { mensagemRevisao, niveisVisuaisRevisao } from '../revisao'

const props = defineProps({
  plano: { type: Object, required: true },
  podeAlterar: Boolean,
})
const emit = defineEmits(['registrar-revisao', 'alterado'])
const edicao = ref(null)
const confirmarExclusao = ref(false)
const ocupado = ref(false)
const erro = ref('')

function editar() {
  erro.value = ''
  confirmarExclusao.value = false
  const { id, equipamentoId, nomePlano, intervaloHoras, descricao } = props.plano
  edicao.value = { id, equipamentoId, nomePlano, intervaloHoras, descricao }
}

function cancelar() {
  edicao.value = null
  confirmarExclusao.value = false
  erro.value = ''
}

async function salvar() {
  ocupado.value = true
  erro.value = ''
  try {
    await api.atualizarPlano(edicao.value)
    edicao.value = null
    emit('alterado', 'Plano atualizado.')
  } catch (falha) {
    erro.value = falha.message
  } finally {
    ocupado.value = false
  }
}

async function excluir() {
  ocupado.value = true
  erro.value = ''
  try {
    await api.excluirPlano(props.plano.id)
    confirmarExclusao.value = false
    emit('alterado', 'Plano e revisões vinculadas excluídos.')
  } catch (falha) {
    erro.value = falha.message
  } finally {
    ocupado.value = false
  }
}
</script>

<template>
  <article class="superficie plano">
    <form @submit.prevent="salvar">
      <div class="plano-cabecalho">
        <div class="plano-titulo">
          <input v-if="edicao" v-model.trim="edicao.nomePlano" aria-label="Nome do plano" maxlength="50" required :disabled="ocupado">
          <template v-else>
            <h3>{{ plano.nomePlano }}</h3>
            <button v-if="podeAlterar" type="button" class="editar" @click="editar">Editar</button>
          </template>
        </div>
        <span class="selo" :class="niveisVisuaisRevisao[plano.statusRevisao]">{{ mensagemRevisao(plano.horasRestantes) }}</span>
      </div>
      <textarea v-if="edicao" v-model.trim="edicao.descricao" aria-label="Descrição do plano" maxlength="500" rows="2" required :disabled="ocupado" />
      <p v-else class="descricao">{{ plano.descricao }}</p>
      <dl class="plano-dados">
        <div>
          <dt>Intervalo</dt>
          <dd v-if="edicao"><input v-model.number="edicao.intervaloHoras" type="number" aria-label="Intervalo em horas" min="0.01" max="99999999.99" step="0.01" required :disabled="ocupado"> h</dd>
          <dd v-else>{{ horas(plano.intervaloHoras) }}</dd>
        </div>
        <div><dt>Última revisão</dt><dd>{{ plano.horimetroUltimaRevisao === null ? 'Não informada' : horas(plano.horimetroUltimaRevisao) }}</dd></div>
        <div><dt>Próxima revisão</dt><dd>{{ plano.proximoHorimetro === null ? 'A definir' : horas(plano.proximoHorimetro) }}</dd></div>
      </dl>
      <div v-if="podeAlterar" class="acoes">
        <template v-if="edicao">
          <button class="principal" :disabled="ocupado">{{ ocupado ? 'Salvando…' : 'Salvar' }}</button>
          <button type="button" class="secundario" :disabled="ocupado" @click="cancelar">Cancelar</button>
        </template>
        <button v-else type="button" @click="emit('registrar-revisao', plano)">Registrar revisão</button>
        <button type="button" class="lixeira" :aria-label="'Excluir plano ' + plano.nomePlano" :disabled="ocupado" @click="cancelar(); confirmarExclusao = true">
          <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M3 6h18M9 6V3h6v3M5 6l1 14h12l1-14M10 10v6m4-6v6"/>
          </svg>
        </button>
      </div>
      <div v-if="confirmarExclusao" class="confirmacao">
        <div class="acoes">
          <button type="button" class="perigo" :disabled="ocupado" @click="excluir">Excluir plano</button>
          <button type="button" class="secundario" :disabled="ocupado" @click="cancelar">Cancelar</button>
        </div>
        <span>Excluir este plano e suas revisões?</span>
      </div>
      <p v-if="erro" class="erro" role="alert">{{ erro }}</p>
    </form>
  </article>
</template>
