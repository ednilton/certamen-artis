'use client';

import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  cssVariables: true,
  palette: {
    primary: { main: '#1565c0' },
    secondary: { main: '#7b1fa2' },
    background: { default: '#f5f7fa' },
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: 'Roboto, "Segoe UI", system-ui, -apple-system, sans-serif',
  },
});

export default theme;
