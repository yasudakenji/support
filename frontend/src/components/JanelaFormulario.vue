<script setup>
import { onMounted, ref } from 'vue'

const props = defineProps({
  titulo: { type: String, required: true },
  confirmar: { type: String, required: true },
  ocupado: Boolean,
  perigo: Boolean,
  erro: String,
})
const emit = defineEmits(['fechar', 'salvar'])
const janela = ref(null)

onMounted(() => janela.value.showModal())

function fechar() {
  if (!props.ocupado) emit('fechar')
}
</script>

<template>
  <dialog ref="janela" aria-labelledby="titulo-janela" @cancel.prevent="fechar">
    <header class="janela-cabecalho">
      <h2 id="titulo-janela">{{ titulo }}</h2>
      <button type="button" class="fechar" aria-label="Fechar janela" :disabled="ocupado" @click="fechar">×</button>
    </header>
    <form @submit.prevent="emit('salvar')">
      <fieldset class="campos" :disabled="ocupado">
        <slot />
      </fieldset>
      <p v-if="erro" class="erro" role="alert">{{ erro }}</p>
      <footer class="janela-acoes">
        <button type="button" class="secundario" :disabled="ocupado" @click="fechar">Cancelar</button>
        <button :class="perigo ? 'perigo' : 'principal'" :disabled="ocupado">
          {{ ocupado ? 'Salvando…' : confirmar }}
        </button>
      </footer>
    </form>
  </dialog>
</template>
