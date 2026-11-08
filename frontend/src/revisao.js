import { horas } from './formatacao'

export const niveisVisuaisRevisao = {
  AVALIACAO_INICIAL_PENDENTE: 'neutro',
  VENCIDA: 'vencido',
  PROXIMA: 'proximo',
  EM_DIA: 'em-dia',
}

export function mensagemRevisao(restante) {
  if (restante === null) return 'Sem revisão registrada'
  if (restante < 0) return 'Vencida há ' + horas(-restante)
  if (restante === 0) return 'Revisão devida agora'
  return 'Faltam ' + horas(restante)
}
