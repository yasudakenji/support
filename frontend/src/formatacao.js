const numero = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 })

export const horas = valor => numero.format(valor) + ' h'

export function data(valor) {
  return valor ? valor.slice(0, 10).split('-').reverse().join('/') : '—'
}

export function dataHora(valor) {
  return valor ? data(valor) + ', ' + valor.slice(11, 16) : '—'
}

export function agoraLocal() {
  const instante = new Date()
  instante.setMinutes(instante.getMinutes() - instante.getTimezoneOffset())
  return instante.toISOString().slice(0, 19)
}
