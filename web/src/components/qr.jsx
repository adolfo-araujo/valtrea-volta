import { useEffect, useState } from 'react'
import QRCode from 'qrcode'

/** Desenha o QR Code no próprio navegador (o endereço não sai para nenhum serviço de terceiros). */
export function useQrCode(url, largura = 512) {
  const [imagem, setImagem] = useState(null)
  useEffect(() => {
    let ativo = true
    if (!url) {
      setImagem(null)
      return undefined
    }
    QRCode.toDataURL(url, { width: largura, margin: 2, errorCorrectionLevel: 'M' })
      .then((dados) => ativo && setImagem(dados))
      .catch(() => ativo && setImagem(null))
    return () => {
      ativo = false
    }
  }, [url, largura])
  return imagem
}

export function QrCode({ url, nome, className = 'h-40 w-40' }) {
  const imagem = useQrCode(url)
  if (!imagem) return <div className={`${className} animate-pulse rounded-lg bg-surface-2`} aria-hidden="true" />
  return <img src={imagem} alt={`QR Code da etiqueta de ${nome}`} className={`${className} rounded-lg bg-white p-1`} />
}
