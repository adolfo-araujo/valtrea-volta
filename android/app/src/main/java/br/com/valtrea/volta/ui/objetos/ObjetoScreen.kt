package br.com.valtrea.volta.ui.objetos

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.valtrea.volta.data.Etiqueta
import br.com.valtrea.volta.data.Objeto
import br.com.valtrea.volta.data.ObjetoRequest
import br.com.valtrea.volta.data.PerdidoRequest
import br.com.valtrea.volta.data.ValtreaApi
import br.com.valtrea.volta.data.mensagemAmigavel
import br.com.valtrea.volta.ui.componentes.BotaoVoltar
import br.com.valtrea.volta.ui.componentes.QrCode
import br.com.valtrea.volta.ui.componentes.dataCurta
import br.com.valtrea.volta.ui.componentes.qrCodeBitmap
import br.com.valtrea.volta.ui.valtreaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObjetoViewModel(private val api: ValtreaApi, private val id: String) : ViewModel() {
    var objeto by mutableStateOf<Objeto?>(null)
        private set
    var etiquetas by mutableStateOf<List<Etiqueta>>(emptyList())
        private set
    var ocupado by mutableStateOf(false)
        private set
    var erro by mutableStateOf<String?>(null)
    var aviso by mutableStateOf<String?>(null)
    var editando by mutableStateOf(false)
    var confirmandoExclusao by mutableStateOf(false)

    fun carregar() = tarefa {
        objeto = api.objeto(id)
        etiquetas = api.etiquetas(id)
    }

    fun alternarPerdido() = tarefa { objeto = api.marcarPerdido(id, PerdidoRequest(!(objeto?.perdido ?: false))) }

    fun gerarEtiqueta() = tarefa {
        api.criarEtiqueta(id)
        etiquetas = api.etiquetas(id)
        objeto = api.objeto(id)
    }

    fun desativar(etiqueta: Etiqueta) = tarefa {
        api.desativarEtiqueta(etiqueta.id)
        etiquetas = api.etiquetas(id)
        objeto = api.objeto(id)
    }

    suspend fun salvar(pedido: ObjetoRequest) {
        objeto = api.atualizarObjeto(id, pedido)
        editando = false
    }

    fun excluir(aoTerminar: () -> Unit) = tarefa {
        api.excluirObjeto(id)
        aoTerminar()
    }

    /** Salva a imagem do QR Code onde a pessoa escolheu (sem pedir permissão de armazenamento). */
    fun salvarImagem(resolver: ContentResolver, destino: Uri, url: String) = tarefa {
        withContext(Dispatchers.IO) {
            val saida = resolver.openOutputStream(destino) ?: error("Não foi possível criar o arquivo.")
            saida.use { qrCodeBitmap(url).compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        aviso = "Imagem salva. Imprima com uns 3 a 5 cm e cole no objeto."
    }

    private fun tarefa(bloco: suspend () -> Unit) {
        if (ocupado) return
        ocupado = true
        erro = null
        viewModelScope.launch {
            runCatching { bloco() }.onFailure { erro = it.mensagemAmigavel() }
            ocupado = false
        }
    }
}

@Composable
fun ObjetoScreen(id: String, aoVoltar: () -> Unit, contentPadding: PaddingValues) {
    val vm = valtreaViewModel { ObjetoViewModel(it.api, id) }
    val contexto = LocalContext.current
    var urlParaSalvar by androidx.compose.runtime.remember { mutableStateOf<String?>(null) }
    val salvarImagem = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val url = urlParaSalvar
        if (uri != null && url != null) vm.salvarImagem(contexto.contentResolver, uri, url)
    }
    LaunchedEffect(id) { vm.carregar() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BotaoVoltar(aoVoltar, Modifier.offset(x = (-12).dp))
        val objeto = vm.objeto
        Text(
            objeto?.nome ?: "Objeto",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        vm.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        vm.aviso?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        if (objeto == null) return@Column

        // Perdido: interruptor com o rótulo junto, lido como uma coisa só pelo TalkBack
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    toggleableState = ToggleableState(objeto.perdido)
                    stateDescription = if (objeto.perdido) "marcado como perdido" else "não está perdido"
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Perdi este objeto", fontWeight = FontWeight.Bold)
                Text(
                    "Quem ler o QR Code vê um destaque pedindo para avisar você.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = objeto.perdido, onCheckedChange = { vm.alternarPerdido() }, enabled = !vm.ocupado)
        }

        Text("Etiquetas com QR Code", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        val ativas = vm.etiquetas.filter { it.ativa && it.url != null }
        if (ativas.isEmpty()) {
            Text(
                "Nenhuma etiqueta ativa. Gere uma, salve a imagem, imprima e cole no objeto.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ativas.forEach { etiqueta ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QrCode(etiqueta.url!!, "QR Code da etiqueta de ${objeto.nome}", Modifier.size(200.dp))
                Text(
                    "Criada em ${etiqueta.criadaEm.dataCurta()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = {
                        urlParaSalvar = etiqueta.url
                        salvarImagem.launch("etiqueta-volta.png")
                    },
                    enabled = !vm.ocupado,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) { Text("Salvar imagem para imprimir") }
                TextButton(onClick = { vm.desativar(etiqueta) }, enabled = !vm.ocupado) {
                    Text("Desativar etiqueta", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        OutlinedButton(
            onClick = vm::gerarEtiqueta,
            enabled = !vm.ocupado,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("Gerar etiqueta nova") }

        Text("Dados do objeto", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        Text("Categoria: ${objeto.categoria.rotulo}")
        Text("Recado para quem encontrar: ${objeto.mensagemPublica ?: "—"}")
        Text("Anotações só suas: ${objeto.descricao ?: "—"}")
        OutlinedButton(onClick = { vm.editando = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Editar") }
        TextButton(onClick = { vm.confirmandoExclusao = true }) {
            Text("Excluir objeto", color = MaterialTheme.colorScheme.error)
        }
    }

    vm.objeto?.let { objeto ->
        if (vm.editando) {
            DialogoObjeto("Editar objeto", objeto, aoCancelar = { vm.editando = false }, aoSalvar = vm::salvar)
        }
        if (vm.confirmandoExclusao) {
            AlertDialog(
                onDismissRequest = { vm.confirmandoExclusao = false },
                title = { Text("Excluir ${objeto.nome}?") },
                text = { Text("Apaga o objeto, as etiquetas (os QR Codes param de funcionar) e os avisos dele.") },
                confirmButton = {
                    TextButton(onClick = { vm.excluir(aoVoltar) }, enabled = !vm.ocupado) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = { TextButton(onClick = { vm.confirmandoExclusao = false }) { Text("Cancelar") } },
            )
        }
    }
}
