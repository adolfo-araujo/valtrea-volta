import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import { AuthProvider } from './lib/auth'
import { AvisoServidorLento } from './components/ui'
import './fonts.css'
import './index.css'
import { TemaNoCanto } from './components/tema'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
        <TemaNoCanto />
        <AvisoServidorLento />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
)

// Deixa o site funcionar como app instalado (e abrir mesmo sem internet, com a última versão salva).
if ('serviceWorker' in navigator && import.meta.env.PROD) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {})
  })
}
