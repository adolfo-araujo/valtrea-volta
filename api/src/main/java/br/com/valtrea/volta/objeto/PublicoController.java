package br.com.valtrea.volta.objeto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.volta.objeto.ObjetoDtos.AvisoPublicoRequest;
import br.com.valtrea.volta.objeto.ObjetoDtos.ObjetoPublicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Rotas abertas, usadas por quem leu o QR Code (sem conta). Têm limite próprio por IP (ProtecaoFilter) e nunca devolvem
 * nada sobre o dono.
 */
@RestController
@RequestMapping("/api/v1/publico")
@Tag(name = "Quem encontrou")
public class PublicoController {

	private final ObjetoService service;

	public PublicoController(ObjetoService service) {
		this.service = service;
	}

	@GetMapping("/{codigo}")
	@Operation(summary = "O que aparece ao ler o QR Code: nome do objeto, categoria e o recado do dono")
	public ObjetoPublicoResponse objeto(@PathVariable String codigo) {
		return service.publico(codigo);
	}

	@PostMapping("/{codigo}/avisos")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(summary = "Avisa o dono que o objeto foi encontrado. O contato é opcional e só o dono vê")
	public void avisar(@PathVariable String codigo, @RequestBody @Valid AvisoPublicoRequest pedido) {
		if (pedido.website() != null && !pedido.website().isBlank()) {
			return; // robô: mesma resposta, nada é gravado
		}
		service.avisar(codigo, pedido);
	}

}
