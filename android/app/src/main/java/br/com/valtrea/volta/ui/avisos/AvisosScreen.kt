package br.com.valtrea.volta.ui.avisos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.valtrea.volta.data.Aviso
import br.com.valtrea.volta.data.StatusAviso
import br.com.valtrea.volta.data.StatusAvisoRequest
import br.com.valtrea.volta.data.ValtreaApi
import br.com.valtrea.volta.data.mensagemAmigavel
import br.com.valtrea.volta.ui.componentes.dataCurta
import br.com.valtrea.volta.ui.valtreaViewModel
import kotlinx.coroutines.launch

class AvisosViewModel(private val api: ValtreaApi) : ViewModel() {
    var avisos by mutableStateOf<List<Aviso>?>(null)
        private set
    var erro by mutableStateOf<String?>(null)
        private set

    fun carregar() = tarefa { avisos = api.avisos() }

    fun marcar(aviso: Aviso, status: StatusAviso) = tarefa {
        val novo = api.marcarAviso(aviso.id, StatusAvisoRequest(status))
        avisos = avisos?.map { if (it.id == novo.id) novo else it }
    }

    fun apagar(aviso: Aviso) = tarefa {
        api.excluirAviso(aviso.id)
        avisos = avisos?.filterNot { it.id == aviso.id }
    }

    private fun tarefa(bloco: suspend () -> Unit) {
        erro = null
        viewModelScope.launch { runCatching { bloco() }.onFailure { erro = it.mensagemAmigavel() } }
    }
}

private val NOMES = mapOf(StatusAviso.NOVO to "Novo", StatusAviso.LIDO to "Lido", StatusAviso.RESOLVIDO to "Resolvido")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvisosScreen(contentPadding: PaddingValues) {
    val vm = valtreaViewModel { AvisosViewModel(it.api) }
    LaunchedEffect(Unit) { vm.carregar() }

    LazyColumn(
        Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Avisos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        }
        item {
            Text(
                "Para a devolução, combine um lugar público e movimentado, de dia, ou um balcão de achados e perdidos. " +
                    "Nunca pague para receber algo de volta. Os avisos são apagados 90 dias depois de chegarem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        vm.erro?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (vm.avisos?.isEmpty() == true) item { Text("Nenhum aviso por enquanto.") }
        items(vm.avisos.orEmpty(), key = { it.id }) { aviso ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(aviso.objetoNome, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                Text(
                    "${NOMES[aviso.status]} · ${aviso.criadoEm.dataCurta()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (aviso.status == StatusAviso.NOVO) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(aviso.mensagem)
                Text(
                    "Contato deixado: ${aviso.contato ?: "nenhum"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (aviso.status == StatusAviso.NOVO) {
                        OutlinedButton(onClick = { vm.marcar(aviso, StatusAviso.LIDO) }) { Text("Marcar como lido") }
                    }
                    if (aviso.status != StatusAviso.RESOLVIDO) {
                        Button(onClick = { vm.marcar(aviso, StatusAviso.RESOLVIDO) }) { Text("Recuperei") }
                    }
                    TextButton(onClick = { vm.apagar(aviso) }) {
                        Text("Apagar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
