package br.com.valtrea.volta.ui.componentes

import android.graphics.Bitmap
import android.graphics.Color as CorAndroid
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** QR Code desenhado no próprio celular, sempre preto no branco (leitores de QR Code esperam esse contraste). */
@Composable
fun QrCode(texto: String, descricao: String, modifier: Modifier = Modifier) {
    val matriz = remember(texto) { gerarMatriz(texto) }
    Canvas(
        modifier
            .background(Color.White)
            .padding(8.dp)
            .semantics { contentDescription = descricao },
    ) {
        val modulo = size.minDimension / matriz.width
        for (y in 0 until matriz.height) {
            for (x in 0 until matriz.width) {
                if (matriz[x, y]) {
                    // Meio pixel a mais evita frestas claras entre módulos vizinhos.
                    drawRect(Color.Black, topLeft = Offset(x * modulo, y * modulo), size = Size(modulo + 0.5f, modulo + 0.5f))
                }
            }
        }
    }
}

/** Imagem do QR Code para salvar e imprimir (com margem branca). */
fun qrCodeBitmap(texto: String, tamanho: Int = 1024): Bitmap {
    val matriz = QRCodeWriter().encode(
        texto,
        BarcodeFormat.QR_CODE,
        tamanho,
        tamanho,
        mapOf(EncodeHintType.MARGIN to 2, EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M),
    )
    val pixels = IntArray(tamanho * tamanho) { i ->
        if (matriz[i % tamanho, i / tamanho]) CorAndroid.BLACK else CorAndroid.WHITE
    }
    return Bitmap.createBitmap(pixels, tamanho, tamanho, Bitmap.Config.ARGB_8888)
}

private fun gerarMatriz(texto: String): BitMatrix = QRCodeWriter().encode(
    texto,
    BarcodeFormat.QR_CODE,
    0,
    0,
    mapOf(EncodeHintType.MARGIN to 0, EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M),
)
