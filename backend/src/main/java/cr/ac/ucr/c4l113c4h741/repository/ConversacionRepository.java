package cr.ac.ucr.c4l113c4h741.repository;

import cr.ac.ucr.c4l113c4h741.model.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversacionRepository extends JpaRepository<Conversacion, UUID> {

    List<Conversacion> findAllByOrderByFechaCreacionDesc();
}
