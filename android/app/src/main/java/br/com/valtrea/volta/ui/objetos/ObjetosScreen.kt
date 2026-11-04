package br.com.valtrea.volta.ui.objetos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.valtrea.volta.data.AceitarTermosRequest
import br.com.valtrea.volta.data.Objeto
import br.com.valtrea.volta.data.ObjetoRequest
import br.com.valtrea.volta.data.Perfil
import br.com.valtrea.volta.data.ValtreaApi
import br.com.valtrea.volta.data.mensagemAmigavel
import br.com.valtrea.volta.ui.componentes.Links
import br.com.valtrea.volta.ui.valtreaViewModel
import kotlinx.coroutines.launch

class ObjetosViewModel(private val api: ValtreaApi) : ViewModel() {
    var objetos by mutableStateOf<List<Objeto>?>(null)
        private set
    var perfil by mutableStateOf<Perfil?>(null)
        private set
    var erro by mutableStateOf<String?>(null)
        private set
    var criando by mutableStateOf(false)

    fun carregar() {
        viewModelScope.launch {
            runCatching { api.perfil() }.onSuccess { perfil = it }
            runCatching { api.objetos() }
                .onSuccess { objetos = it; erro = null }
                .onFailure { erro = it.mensagemAmigavel() }
        }
    }

    fun aceitarTermos() {
        viewModelScope.launch {
            runCatching { api.aceitarTermos(AceitarTermosRequest(Links.VERSAO_TERMOS)) }
                .onSuccess { perfil = it }
                .onFailure { erro = it.mensagemAmigavel() }
        }
    }

    suspend fun criar(pedido: ObjetoRequest): Objeto = api.criarObjeto(pedido)
}

@Composable
fun ObjetosScreen(
    aoAbrir: (String) -> Unit,
    aoConfirmarEmail: () -> Unit,
    contentPadding: PaddingValues,
) {
    val vm = valtreaViewModel { ObjetosViewModel(it.api) }
    LaunchedEffect(Unit) { vm.carregar() }

    LazyColumn(
        Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Meus objetos",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
        }
        vm.perfil?.let { p ->
            if (!p.termosAceitos) item { AvisoNovosTermos(aoAceitar = vm::aceitarTermos) }
            if (!p.emailConfirmado) item { AvisoConfirmarEmail(aoConfirmarEmail) }
        }
        vm.erro?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        item {
            Button(onClick = { vm.criando = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Cadastrar objeto")
            }
        }
        val lista = vm.objetos
        if (lista != null && lista.isEmpty()) {
            item {
                Text(
                    "Comece pelo objeto que você mais teme perder. Cadastre, gere a etiqueta com QR Code e cole. Quem " +
                        "encontrar lê o código e avisa você, sem ver seu telefone nem seu endereço.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(lista.orEmpty(), key = { it.id }) { objeto -> LinhaObjeto(objeto) { aoAbrir(objeto.id) } }
    }

    if (vm.criando) {
        DialogoObjeto(
            titulo = "Novo objeto",
            inicial = null,
            aoCancelar = { vm.criando = false },
            aoSalvar = { pedido ->
                val novo = vm.criar(pedido)
                vm.criando = false
                aoAbrir(novo.id)
            },
        )
    }
}

@Composable
private fun LinhaObjeto(objeto: Objeto, aoClicar: () -> Unit) {
    val etiquetas = if (objeto.etiquetasAtivas == 1L) "1 etiqueta" else "${objeto.etiquetasAtivas} etiquetas"
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClickLabel = "Abrir ${objeto.nome}", role = Role.Button, onClick = aoClicar)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(objeto.nome, fontWeight = FontWeight.Bold)
            Text(
                "${objeto.categoria.rotulo} · $etiquetas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (objeto.perdido) Text("Marcado como perdido", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            if (objeto.avisosNovos > 0) {
                Text(
                    if (objeto.avisosNovos == 1L) "1 aviso novo" else "${objeto.avisosNovos} avisos novos",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

@Composable
private fun AvisoConfirmarEmail(aoConfirmar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = aoConfirmar)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.MarkEmailUnread, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(Modifier.width(12.dp))
        Text(
            "Confirme seu e-mail para gerar etiquetas: é por ele que avisamos quando alguém encontrar.",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Aviso de nova versão dos Termos e da Política, com o novo aceite. */
@Composable
internal fun AvisoNovosTermos(aoAceitar: () -> Unit) {
    val contexto = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "Atualizamos os Termos de uso e a Política de privacidade",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            "Se não concordar, você pode excluir sua conta em Conta.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { Links.abrir(contexto, Links.termos) }) { Text("Termos") }
            TextButton(onClick = { Links.abrir(contexto, Links.privacidade) }) { Text("Privacidade") }
            Spacer(Modifier.weight(1f))
            Button(onClick = aoAceitar) { Text("Li e concordo") }
        }
    }
}
