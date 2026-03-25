import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('react') || id.includes('react-router')) return 'vendor-react';
            if (id.includes('@reduxjs') || id.includes('react-redux')) return 'vendor-redux';
            if (id.includes('recharts')) return 'vendor-charts';
            if (id.includes('axios')) return 'vendor-axios';
          }
        },
      },
    },
  },
})
