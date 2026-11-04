package br.com.valtrea.volta.ui.objetos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.valtrea.volta.data.Categoria
import br.com.valtrea.volta.data.Objeto
import br.com.valtrea.volta.data.ObjetoRequest
import br.com.valtrea.volta.data.mensagemAmigavel
import kotlinx.coroutines.launch

/** Cadastro e edição de objeto. [inicial] nulo = objeto novo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoObjeto(
    titulo: String,
    inicial: Objeto?,
    aoCancelar: () -> Unit,
    aoSalvar: suspend (ObjetoRequest) -> Unit,
) {
    var nome by remember { mutableStateOf(inicial?.nome ?: "") }
    var categoria by remember { mutableStateOf(inicial?.categoria ?: Categoria.MOCHILA_BOLSA) }
    var recado by remember { mutableStateOf(inicial?.mensagemPublica ?: "") }
    var anotacoes by remember { mutableStateOf(inicial?.descricao ?: "") }
    var aberto by remember { mutableStateOf(false) }
    var salvando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    val escopo = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!salvando) aoCancelar() },
        title = { Text(titulo) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { if (it.length <= 80) nome = it },
                    label = { Text("Nome do objeto") },
                    placeholder = { Text("Ex.: Mochila preta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ExposedDropdownMenuBox(expanded = aberto, onExpandedChange = { aberto = it }) {
                    OutlinedTextField(
                        value = categoria.rotulo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                        Categoria.entries.forEach { c ->
                            DropdownMenuItem(text = { Text(c.rotulo) }, onClick = { categoria = c; aberto = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = recado,
                    onValueChange = { if (it.length <= 200) recado = it },
                    label = { Text("Recado para quem encontrar (opcional)") },
                    supportingText = { Text("Aparece para quem ler o QR Code. Não coloque telefone nem endereço.") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = anotacoes,
                    onValueChange = { if (it.length <= 500) anotacoes = it },
                    label = { Text("Anotações só suas (opcional)") },
                    supportingText = { Text("Ninguém mais vê.") },
                    modifier = Modifier.fillMaxWidth(),
                )
                erro?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !salvando && nome.isNotBlank(),
                onClick = {
                    salvando = true
                    erro = null
                    escopo.launch {
                        runCatching {
                            aoSalvar(
                                ObjetoRequest(
                                    nome = nome.trim(),
                                    categoria = categoria,
                                    descricao = anotacoes.trim().ifBlank { null },
                                    mensagemPublica = recado.trim().ifBlank { null },
                                ),
                            )
                        }.onFailure { erro = it.mensagemAmigavel() }
                        salvando = false
                    }
                },
            ) {
                if (salvando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = aoCancelar, enabled = !salvando) { Text("Cancelar") } },
    )
}
