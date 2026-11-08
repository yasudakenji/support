<script setup>
import { ref } from 'vue'
import { api } from '../api'

const emit = defineEmits(['autenticado'])
const usuario = ref('')
const senha = ref('')
const entrando = ref(false)
const erro = ref('')

async function entrar() {
  entrando.value = true
  erro.value = ''
  try {
    const conta = await api.entrar(usuario.value, senha.value)
    emit('autenticado', conta)
  } catch (falha) {
    erro.value = falha.message
  } finally {
    senha.value = ''
    entrando.value = false
  }
}
</script>

<template>
  <form class="superficie tela-login" @submit.prevent="entrar">
    <h1>Entrar no Support</h1>
    <label>Usuário ou e-mail<input v-model.trim="usuario" autocomplete="username" required :disabled="entrando"></label>
    <label>Senha<input v-model="senha" type="password" autocomplete="current-password" required :disabled="entrando"></label>
    <p v-if="erro" class="erro" role="alert">{{ erro }}</p>
    <button type="submit" class="principal" :disabled="entrando">{{ entrando ? 'Entrando…' : 'Entrar' }}</button>
  </form>
</template>
