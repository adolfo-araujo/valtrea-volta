package br.com.valtrea.volta.objeto;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.volta.objeto.ObjetoDtos.AvisoResponse;
import br.com.valtrea.volta.objeto.ObjetoDtos.EtiquetaResponse;
import br.com.valtrea.volta.objeto.ObjetoDtos.ObjetoRequest;
import br.com.valtrea.volta.objeto.ObjetoDtos.ObjetoResponse;
import br.com.valtrea.volta.objeto.ObjetoDtos.PerdidoRequest;
import br.com.valtrea.volta.objeto.ObjetoDtos.StatusAvisoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Objetos")
public class ObjetoController {

	private final ObjetoService service;

	public ObjetoController(ObjetoService service) {
		this.service = service;
	}

	private static UUID dono(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}

	@GetMapping("/objetos")
	@Operation(summary = "Meus objetos, do mais recente para o mais antigo")
	public List<ObjetoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
		return service.listar(dono(jwt));
	}

	@PostMapping("/objetos")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um objeto (até 100 por conta)")
	public ObjetoResponse criar(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid ObjetoRequest pedido) {
		return service.criar(dono(jwt), pedido);
	}

	@GetMapping("/objetos/{id}")
	@Operation(summary = "Detalhes de um objeto meu")
	public ObjetoResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return service.buscar(dono(jwt), id);
	}

	@PutMapping("/objetos/{id}")
	@Operation(summary = "Altera nome, categoria, descrição e mensagem para quem encontrar")
	public ObjetoResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestBody @Valid ObjetoRequest pedido) {
		return service.atualizar(dono(jwt), id, pedido);
	}

	@PostMapping("/objetos/{id}/perdido")
	@Operation(summary = "Marca ou desmarca o objeto como perdido (a página do QR Code mostra o destaque)")
	public ObjetoResponse perdido(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestBody PerdidoRequest pedido) {
		return service.marcarPerdido(dono(jwt), id, pedido.perdido());
	}

	@DeleteMapping("/objetos/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui o objeto, as etiquetas e os avisos dele")
	public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		service.excluir(dono(jwt), id);
	}

	@GetMapping("/objetos/{id}/etiquetas")
	@Operation(summary = "Etiquetas do objeto, com o endereço do QR Code das que estão ativas")
	public List<EtiquetaResponse> etiquetas(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return service.etiquetas(dono(jwt), id);
	}

	@PostMapping("/objetos/{id}/etiquetas")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cria uma etiqueta nova (código aleatório de 256 bits; até 5 ativas por objeto)")
	public EtiquetaResponse criarEtiqueta(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return service.criarEtiqueta(dono(jwt), id);
	}

	@PostMapping("/etiquetas/{id}/desativar")
	@Operation(summary = "Desativa uma etiqueta: o QR Code para de funcionar na hora")
	public EtiquetaResponse desativar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return service.desativarEtiqueta(dono(jwt), id);
	}

	@GetMapping("/avisos")
	@Operation(summary = "Avisos de quem encontrou meus objetos")
	public List<AvisoResponse> avisos(@AuthenticationPrincipal Jwt jwt) {
		return service.avisos(dono(jwt));
	}

	@PostMapping("/avisos/{id}/status")
	@Operation(summary = "Marca o aviso como lido ou resolvido")
	public AvisoResponse marcar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestBody @Valid StatusAvisoRequest pedido) {
		return service.marcarAviso(dono(jwt), id, pedido.status());
	}

	@DeleteMapping("/avisos/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Apaga o aviso (e o contato de quem encontrou)")
	public void excluirAviso(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		service.excluirAviso(dono(jwt), id);
	}

}
