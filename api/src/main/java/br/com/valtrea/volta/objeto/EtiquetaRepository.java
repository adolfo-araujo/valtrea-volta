package br.com.valtrea.volta.objeto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, UUID> {

	@EntityGraph(attributePaths = { "objeto", "objeto.dono" })
	Optional<Etiqueta> findByTokenHash(String tokenHash);

	List<Etiqueta> findByObjetoIdOrderByCriadaEmDesc(UUID objetoId);

	long countByObjetoIdAndDesativadaEmIsNull(UUID objetoId);

}
