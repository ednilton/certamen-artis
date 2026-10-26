'use client';

import { useCallback, useEffect, useState } from 'react';
import { ApiError, eventsApi, type EventResponse } from '@/lib/api';

/**
 * Carrega a lista de eventos da API e expõe estado de carregamento/erro.
 */
export function useEvents() {
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const data = await eventsApi.list();
      setEvents(data);
      setError(null);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Erro inesperado ao carregar os eventos.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const refresh = useCallback(async () => {
    setLoading(true);
    await load();
  }, [load]);

  return { events, loading, error, refresh };
}
