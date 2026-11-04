package br.com.valtrea.volta.objeto;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvisoRepository extends JpaRepository<Aviso, UUID> {

	@EntityGraph(attributePaths = "objeto")
	List<Aviso> findByObjetoDonoIdOrderByCriadoEmDesc(UUID donoId);

	long countByObjetoDonoIdAndStatus(UUID donoId, Aviso.Status status);

	long countByObjetoIdAndStatus(UUID objetoId, Aviso.Status status);

}
