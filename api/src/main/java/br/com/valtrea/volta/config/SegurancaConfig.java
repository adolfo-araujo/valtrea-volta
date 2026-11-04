package br.com.valtrea.volta.config;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class SegurancaConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, ROTAS_PUBLICAS).permitAll()
						.requestMatchers(HttpMethod.GET, "/", "/api/v1/publico/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/publico/*/avisos").permitAll()
						.requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
						.permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth -> oauth
						.bearerTokenResolver(ignorarTokenNasRotasPublicas())
						.jwt(Customizer.withDefaults()))
				.build();
	}

	private static final String[] ROTAS_PUBLICAS = { "/api/v1/auth/cadastro", "/api/v1/auth/login",
			"/api/v1/auth/renovar", "/api/v1/auth/sair", "/api/v1/auth/esqueci-senha", "/api/v1/auth/redefinir-senha" };

	/**
	 * Um token de acesso vencido no cabeçalho faria até as rotas públicas responderem 401, e o app não conseguiria
	 * renovar a sessão. Nessas rotas o cabeçalho é ignorado.
	 */
	private static BearerTokenResolver ignorarTokenNasRotasPublicas() {
		DefaultBearerTokenResolver padrao = new DefaultBearerTokenResolver();
		Set<String> publicas = Set.of(ROTAS_PUBLICAS);
		return request -> publicas.contains(request.getRequestURI())
				|| request.getRequestURI().startsWith("/api/v1/publico/") ? null : padrao.resolve(request);
	}

	/**
	 * O site (volta.valtrea.com.br) chama a API pelo navegador. Só os endereços listados em VALTREA_CORS podem; o app
	 * Android não precisa de CORS. Sem cookies: o login vai no cabeçalho Authorization.
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${valtrea.cors:https://volta.valtrea.com.br}") String origens) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.stream(origens.split(",")).map(String::strip).filter(o -> !o.isEmpty()).toList());
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
		config.setExposedHeaders(List.of("Retry-After", "Content-Disposition"));
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
		fonte.registerCorsConfiguration("/api/**", config);
		return fonte;
	}

	@Bean
	SecretKey chaveJwt(ValtreaProperties propriedades) {
		byte[] bytes = propriedades.jwt().segredo().getBytes(StandardCharsets.UTF_8);
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey chaveJwt) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey chaveJwt) {
		return NimbusJwtDecoder.withSecretKey(chaveJwt).macAlgorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}
