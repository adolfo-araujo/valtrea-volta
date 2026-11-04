package br.com.valtrea.volta.objeto;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjetoRepository extends JpaRepository<Objeto, UUID> {

	List<Objeto> findByDonoIdOrderByCriadoEmDesc(UUID donoId);

	long countByDonoId(UUID donoId);

}
