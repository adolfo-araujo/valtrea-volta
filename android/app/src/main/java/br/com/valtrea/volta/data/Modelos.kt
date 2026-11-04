package br.com.valtrea.volta.data

import kotlinx.serialization.Serializable

// Espelham os DTOs da API (api/src/main/java/br/com/valtrea/volta).

@Serializable
data class CadastroRequest(val nome: String, val email: String, val senha: String)

@Serializable
data class LoginRequest(val email: String, val senha: String)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val tipo: String,
    val expiraEm: String,
    val refreshToken: String,
)

@Serializable
data class RenovarRequest(val refreshToken: String)

@Serializable
data class CodigoRequest(val codigo: String)

@Serializable
data class EsqueciSenhaRequest(val email: String)

@Serializable
data class RedefinirSenhaRequest(val email: String, val codigo: String, val novaSenha: String)

@Serializable
data class Perfil(
    val id: String,
    val nome: String,
    val email: String,
    val emailConfirmado: Boolean,
    /** Falso quando há Termos novos para aceitar: a lista de objetos mostra o aviso. */
    val termosAceitos: Boolean = true,
    /** Conta de administração: a tela Conta mostra o atalho para o painel. */
    val admin: Boolean = false,
)

@Serializable
data class AceitarTermosRequest(val versao: String)

@Serializable
data class AtualizarPerfilRequest(val nome: String)

@Serializable
data class ExcluirContaRequest(val senha: String)

/** Categorias da API, com o nome mostrado nas telas. */
@Serializable
enum class Categoria(val rotulo: String) {
    MOCHILA_BOLSA("Mochila ou bolsa"),
    CARTEIRA_DOCUMENTOS("Carteira ou documentos"),
    CHAVES("Chaves"),
    ELETRONICO("Eletrônico"),
    OCULOS("Óculos"),
    ROUPA("Roupa"),
    BRINQUEDO("Brinquedo"),
    BICICLETA("Bicicleta"),
    MALA("Mala"),
    PET("Pet (coleira)"),
    OUTRO("Outro"),
}

@Serializable
data class Objeto(
    val id: String,
    val nome: String,
    val categoria: Categoria,
    val descricao: String? = null,
    val mensagemPublica: String? = null,
    val perdido: Boolean = false,
    val criadoEm: String,
    val etiquetasAtivas: Long = 0,
    val avisosNovos: Long = 0,
)

@Serializable
data class ObjetoRequest(
    val nome: String,
    val categoria: Categoria,
    val descricao: String? = null,
    val mensagemPublica: String? = null,
)

@Serializable
data class PerdidoRequest(val perdido: Boolean)

/** [url] é o que vai no QR Code; só vem nas etiquetas ativas. */
@Serializable
data class Etiqueta(
    val id: String,
    val url: String? = null,
    val ativa: Boolean,
    val criadaEm: String,
    val desativadaEm: String? = null,
)

@Serializable
enum class StatusAviso { NOVO, LIDO, RESOLVIDO }

@Serializable
data class Aviso(
    val id: String,
    val objetoId: String,
    val objetoNome: String,
    val mensagem: String,
    val contato: String? = null,
    val status: StatusAviso,
    val criadoEm: String,
)

@Serializable
data class StatusAvisoRequest(val status: StatusAviso)

/** Painel de administração: só números agregados, nenhum dado de outra pessoa. */
@Serializable
data class MetricasAdmin(
    val geradoEm: String,
    val contas: ContasAdmin,
    val uso: UsoAdmin,
    val cadastrosPorSemana: List<SemanaAdmin>,
)

@Serializable
data class ContasAdmin(
    val total: Long,
    val confirmadas: Long,
    val novas7Dias: Long,
    val novas30Dias: Long,
    val comSessaoAtiva: Long,
)

@Serializable
data class UsoAdmin(
    val objetos: Long,
    val contasComObjetos: Long,
    val etiquetasAtivas: Long,
    val objetosPerdidos: Long,
    val avisos7Dias: Long,
    val avisos30Dias: Long,
    val avisosResolvidos: Long,
)

@Serializable
data class SemanaAdmin(val semana: String, val cadastros: Long)

/** Resposta de erro da API no formato ProblemDetail. */
@Serializable
data class Problema(val detail: String? = null, val campos: Map<String, String>? = null)
