/**
 * Cliente HTTP da API de eventos.
 * Centraliza URL base, serialização e tradução dos erros (ProblemDetail) da API.
 */

export type EventType = 'PRODUCTION' | 'QUALITY';

export interface EventResponse {
  id: string;
  eventId: string;
  eventType: EventType;
  description: string | null;
  eventTime: string;
  createdAt: string;
}

export interface CreateEventRequest {
  eventId: string;
  eventType: EventType;
  description: string;
  eventTime: string;
}

/** Formato de erro devolvido pela API (RFC 9457). */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  invalid_fields?: Record<string, string>;
}

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail;

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `Erro ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;

  try {
    response = await fetch(`${API_URL}${path}`, {
      ...init,
      headers: { 'Content-Type': 'application/json', ...init?.headers },
      cache: 'no-store',
    });
  } catch {
    // Falha de rede: API fora do ar, CORS bloqueado, URL errada etc.
    throw new ApiError(0, {
      title: 'Sem conexão',
      detail: 'Não foi possível conectar à API. Verifique se o backend está rodando.',
    });
  }

  if (!response.ok) {
    const problem = (await response.json().catch(() => ({}))) as ProblemDetail;
    throw new ApiError(response.status, { status: response.status, ...problem });
  }

  return (await response.json()) as T;
}

export const eventsApi = {
  list: () => request<EventResponse[]>('/api/events'),

  create: (payload: CreateEventRequest) =>
    request<EventResponse>('/api/events', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
};
