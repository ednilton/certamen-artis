import type { EventType } from '@/lib/api';

const dateTimeFormatter = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'medium',
});

/** Exibe uma data ISO (UTC) no fuso e formato do navegador. */
export function formatDateTime(iso: string): string {
  return dateTimeFormatter.format(new Date(iso));
}

/**
 * Converte o valor de um <input type="datetime-local"> (horário local, sem fuso)
 * para ISO-8601 em UTC, que é o formato que a API espera.
 */
export function localInputToIso(value: string): string {
  return new Date(value).toISOString();
}

/** Sugere um ID legível e praticamente único para o evento. */
export function generateEventId(): string {
  return `EVT-${Date.now().toString().slice(-8)}`;
}

export const EVENT_TYPE_LABELS: Record<EventType, string> = {
  PRODUCTION: 'Produção',
  QUALITY: 'Qualidade',
};
