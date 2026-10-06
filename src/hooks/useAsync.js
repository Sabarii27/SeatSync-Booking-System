import { useEffect, useState, useCallback } from 'react';
import { errMsg } from '../services/api.js';

export default function useAsync(fn, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const run = useCallback(async () => {
    setLoading(true);
    setError('');
    try { setData(await fn()); } catch (e) { setError(errMsg(e)); } finally { setLoading(false); }
  }, deps); // eslint-disable-line
  useEffect(() => { run(); }, [run]);
  return { data, loading, error, reload: run, setData };
}
