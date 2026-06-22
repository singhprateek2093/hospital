import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite dev server on :5173. The backend's CORS config already allows this origin,
// so the browser can call http://localhost:8080 directly (see src/api/client.js).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
})
