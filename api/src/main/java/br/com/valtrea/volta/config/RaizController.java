package br.com.valtrea.volta.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Quem abre o endereço da API no navegador cai na documentação, em vez de um 401. Os Termos, a Política e as demais
 * páginas ficam no site (volta.valtrea.com.br).
 */
@Controller
@Hidden
public class RaizController {

	@GetMapping("/")
	public String raiz() {
		return "redirect:/swagger-ui.html";
	}

}
