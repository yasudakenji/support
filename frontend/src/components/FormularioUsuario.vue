<script setup>
import { ref } from 'vue'
import JanelaFormulario from './JanelaFormulario.vue'

defineProps({
  ocupado: Boolean,
  erro: String,
})

const emit = defineEmits(['fechar', 'salvar'])
const dados = ref({ nome: '', email: '', senha: '', perfil: 'USUARIO' })

function salvar() {
  emit('salvar', { ...dados.value })
}
</script>

<template>
  <JanelaFormulario titulo="Cadastrar usuário" confirmar="Cadastrar usuário"
    :ocupado="ocupado" :erro="erro" @fechar="emit('fechar')" @salvar="salvar">
    <label class="largura-total">Nome<input v-model.trim="dados.nome" required maxlength="50" autofocus></label>
    <label class="largura-total">E-mail<input v-model.trim="dados.email" type="email" required maxlength="254"></label>
    <label>Senha<input v-model="dados.senha" type="password" required minlength="8" maxlength="72" autocomplete="new-password"></label>
    <label>Perfil
      <select v-model="dados.perfil" aria-label="Perfil">
        <option value="USUARIO">Somente consulta</option>
        <option value="ADMIN">Administrador</option>
      </select>
    </label>
  </JanelaFormulario>
</template>
