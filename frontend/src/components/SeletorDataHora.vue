<script setup>
import { computed, ref, useId } from 'vue'
import { agoraLocal, data } from '../formatacao'

const valor = defineModel({ type: String, required: true })
const calendarioId = useId()
const hoje = ref(agoraLocal().slice(0, 10))
const mes = ref(Number(valor.value.slice(5, 7)) - 1)
const ano = ref(Number(valor.value.slice(0, 4)))
const nomesMeses = Array.from({ length: 12 }, (_, indice) =>
  new Intl.DateTimeFormat('pt-BR', { month: 'long', timeZone: 'UTC' }).format(Date.UTC(2024, indice, 1)),
)
const diasSemana = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']
const doisDigitos = numero => String(numero).padStart(2, '0')
const horario = computed(() => valor.value.slice(11, 16))

const dias = computed(() => {
  const primeiro = new Date(0)
  primeiro.setUTCFullYear(ano.value, mes.value, 1)
  const dataDia = new Date(primeiro)
  dataDia.setUTCDate(1 - primeiro.getUTCDay())

  return Array.from({ length: 42 }, () => {
    const anoDia = dataDia.getUTCFullYear()
    const mesDia = dataDia.getUTCMonth()
    const dia = dataDia.getUTCDate()
    const iso = String(anoDia).padStart(4, '0') + '-' + doisDigitos(mesDia + 1) + '-' + doisDigitos(dia)
    dataDia.setUTCDate(dia + 1)
    return { dia, iso, outroMes: mesDia !== mes.value, bloqueado: anoDia < 1 || anoDia > 9999 || iso > hoje.value }
  })
})

function navegarMes(deslocamento) {
  const total = ano.value * 12 + mes.value + deslocamento
  ano.value = Math.floor(total / 12)
  mes.value = total % 12
}

function definirAno(evento) {
  const campo = evento.target
  if (campo.valueAsNumber >= 1 && campo.valueAsNumber <= 9999 && campo.validity.valid) {
    ano.value = campo.valueAsNumber
  }
  campo.value = ano.value
}

function escolherDia(dia) {
  valor.value = dia + valor.value.slice(10)
  mes.value = Number(dia.slice(5, 7)) - 1
  ano.value = Number(dia.slice(0, 4))
}

function alterarHora(parte, numero) {
  const [hora, minuto] = horario.value.split(':').map(Number)
  const novaHora = parte === 'hora' ? numero : hora
  const novoMinuto = parte === 'minuto' ? numero : minuto
  valor.value = valor.value.slice(0, 11) + doisDigitos(novaHora) + ':' + doisDigitos(novoMinuto) + ':00'
}

function digitarHora(parte, evento) {
  const campo = evento.target
  if (campo.value !== '' && campo.validity.valid) alterarHora(parte, campo.valueAsNumber)
  campo.value = parte === 'hora' ? horario.value.slice(0, 2) : horario.value.slice(3, 5)
}
</script>

<template>
  <div class="seletor-data-hora">
    <span class="rotulo-data">Data e hora da leitura</span>
    <button type="button" class="abrir-calendario" :popovertarget="calendarioId" :aria-label="'Data e hora da leitura: ' + data(valor) + ' ' + horario">
      <span>{{ data(valor) }}&ensp;{{ horario }}</span>
      <svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.75">
        <rect x="3" y="5" width="18" height="16" rx="2" />
        <path d="M7 3v4m10-4v4M3 10h18m-14 4h2m2 0h2m2 0h2m-10 3h2m2 0h2" />
      </svg>
    </button>
    <div :id="calendarioId" class="calendario" popover="auto" @beforetoggle="hoje = agoraLocal().slice(0, 10)">
      <div class="navegacao">
        <button type="button" aria-label="Mês anterior" :disabled="ano === 1 && mes === 0" @click="navegarMes(-1)">‹</button>
        <select v-model="mes" aria-label="Mês">
          <option v-for="(nome, indice) in nomesMeses" :key="nome" :value="indice">{{ nome }}</option>
        </select>
        <input type="number" min="1" max="9999" :value="ano" aria-label="Ano"
          @change="definirAno" @keydown.enter.prevent="$event.target.blur()">
        <button type="button" aria-label="Próximo mês" :disabled="ano === 9999 && mes === 11" @click="navegarMes(1)">›</button>
      </div>
      <div class="grade-dias" role="group" aria-label="Dias do mês">
        <span v-for="nome in diasSemana" :key="nome">{{ nome }}</span>
        <button v-for="item in dias" :key="item.iso" type="button" :class="{ 'outro-mes': item.outroMes }"
          :disabled="item.bloqueado" :aria-label="data(item.iso)"
          :aria-pressed="item.iso === valor.slice(0, 10)" @click="escolherDia(item.iso)">{{ item.dia }}</button>
      </div>
      <div class="horario">
        <input type="number" min="0" max="23" inputmode="numeric" :value="horario.slice(0, 2)" aria-label="Hora"
          @change="digitarHora('hora', $event)" @keydown.enter.prevent="$event.target.blur()">
        <span aria-hidden="true">:</span>
        <input type="number" min="0" max="59" inputmode="numeric" :value="horario.slice(3, 5)" aria-label="Minuto"
          @change="digitarHora('minuto', $event)" @keydown.enter.prevent="$event.target.blur()">
      </div>
    </div>
  </div>
</template>

<style scoped>
.seletor-data-hora { min-width: 0; }
.rotulo-data { display: block; margin-bottom: 5px; color: var(--suave); font-size: 13px; }
.abrir-calendario {
  display: flex;
  width: 100%;
  min-height: 42px;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 8px 10px;
  border: 1px solid var(--borda);
  border-radius: 7px;
  cursor: pointer;
}
.abrir-calendario svg { flex: none; color: var(--verde); }
.calendario {
  width: min(calc(100dvw - 24px), 310px);
  max-height: calc(100dvh - 24px);
  padding: 10px;
  border: 1px solid var(--borda);
  border-radius: 8px;
  background: var(--fundo);
  color: var(--texto);
  box-shadow: 0 10px 28px rgb(0 0 0 / 25%);
}
@supports (position-area: block-end span-inline-start) {
  @media (min-height: 640px) {
    .calendario {
      position-area: block-end span-inline-start;
      justify-self: end;
      margin: 6px 8px;
      position-try-fallbacks: flip-block, block-end span-all, block-start span-all;
    }
  }
}
.navegacao { display: grid; grid-template-columns: auto minmax(0, 1fr) 67px auto; gap: 5px; }
.navegacao :is(button, input, select) { padding: 4px 6px; font-size: 14px; }
.navegacao select { text-transform: capitalize; }
.grade-dias { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 3px; margin-top: 8px; text-align: center; }
.grade-dias span { color: var(--suave); font-size: 12px; }
.grade-dias button { padding: 5px 0; border-color: transparent; background: transparent; font-size: 13px; }
.grade-dias button:not(:disabled):hover { background: var(--fundo-verde); }
.grade-dias button:disabled { cursor: not-allowed; opacity: .4; }
.grade-dias button.outro-mes { color: var(--suave); }
.grade-dias button[aria-pressed="true"] { background: var(--verde); color: var(--sobre-verde); }
.horario {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  margin-top: 9px;
  padding-top: 9px;
  border-top: 1px solid var(--borda);
}
.horario input { width: 65px; padding: 5px 7px; text-align: center; }
.horario input::-webkit-inner-spin-button { opacity: 1; }
</style>
