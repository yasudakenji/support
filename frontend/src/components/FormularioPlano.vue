<script setup>
import { ref } from 'vue'
import JanelaFormulario from './JanelaFormulario.vue'

defineProps({
  ocupado: Boolean,
  erro: String,
})

const emit = defineEmits(['fechar', 'salvar'])
const dados = ref({ nomePlano: '', intervaloHoras: '', descricao: '' })

function salvar() {
  emit('salvar', { ...dados.value })
}
</script>

<template>
  <JanelaFormulario titulo="Novo plano de manutenção" confirmar="Criar plano"
    :ocupado="ocupado" :erro="erro" @fechar="emit('fechar')" @salvar="salvar">
    <label>Nome do plano<input v-model.trim="dados.nomePlano" required maxlength="50" autofocus></label>
    <label>Intervalo entre revisões (h)<input v-model.number="dados.intervaloHoras" type="number" required min="0.01" max="99999999.99" step="0.01"></label>
    <label class="largura-total">Descrição e instruções<textarea v-model.trim="dados.descricao" required maxlength="500" rows="3" /></label>
  </JanelaFormulario>
</template>
