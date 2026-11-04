package br.com.valtrea.volta.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ValtreaApi {

    @POST("api/v1/auth/cadastro")
    suspend fun cadastrar(@Body pedido: CadastroRequest): TokenResponse

    @POST("api/v1/auth/login")
    suspend fun entrar(@Body pedido: LoginRequest): TokenResponse

    @POST("api/v1/auth/sair")
    suspend fun sair(@Body pedido: RenovarRequest)

    @POST("api/v1/auth/confirmar-email")
    suspend fun confirmarEmail(@Body pedido: CodigoRequest)

    @POST("api/v1/auth/reenviar-confirmacao")
    suspend fun reenviarConfirmacao()

    @POST("api/v1/auth/esqueci-senha")
    suspend fun esqueciSenha(@Body pedido: EsqueciSenhaRequest)

    @POST("api/v1/auth/redefinir-senha")
    suspend fun redefinirSenha(@Body pedido: RedefinirSenhaRequest)

    @GET("api/v1/eu")
    suspend fun perfil(): Perfil

    @PATCH("api/v1/eu")
    suspend fun atualizarPerfil(@Body pedido: AtualizarPerfilRequest): Perfil

    @POST("api/v1/eu/termos")
    suspend fun aceitarTermos(@Body pedido: AceitarTermosRequest): Perfil

    /** "Baixar meus dados": arquivo JSON com cadastro, objetos, etiquetas e avisos (LGPD). */
    @GET("api/v1/eu/dados")
    suspend fun meusDados(): okhttp3.ResponseBody

    @HTTP(method = "DELETE", path = "api/v1/eu", hasBody = true)
    suspend fun excluirConta(@Body pedido: ExcluirContaRequest)

    @GET("api/v1/admin/metricas")
    suspend fun metricasAdmin(): MetricasAdmin

    @GET("api/v1/objetos")
    suspend fun objetos(): List<Objeto>

    @POST("api/v1/objetos")
    suspend fun criarObjeto(@Body pedido: ObjetoRequest): Objeto

    @GET("api/v1/objetos/{id}")
    suspend fun objeto(@Path("id") id: String): Objeto

    @PUT("api/v1/objetos/{id}")
    suspend fun atualizarObjeto(@Path("id") id: String, @Body pedido: ObjetoRequest): Objeto

    @POST("api/v1/objetos/{id}/perdido")
    suspend fun marcarPerdido(@Path("id") id: String, @Body pedido: PerdidoRequest): Objeto

    @DELETE("api/v1/objetos/{id}")
    suspend fun excluirObjeto(@Path("id") id: String)

    @GET("api/v1/objetos/{id}/etiquetas")
    suspend fun etiquetas(@Path("id") id: String): List<Etiqueta>

    @POST("api/v1/objetos/{id}/etiquetas")
    suspend fun criarEtiqueta(@Path("id") id: String): Etiqueta

    @POST("api/v1/etiquetas/{id}/desativar")
    suspend fun desativarEtiqueta(@Path("id") id: String): Etiqueta

    @GET("api/v1/avisos")
    suspend fun avisos(): List<Aviso>

    @POST("api/v1/avisos/{id}/status")
    suspend fun marcarAviso(@Path("id") id: String, @Body pedido: StatusAvisoRequest): Aviso

    @DELETE("api/v1/avisos/{id}")
    suspend fun excluirAviso(@Path("id") id: String)
}
