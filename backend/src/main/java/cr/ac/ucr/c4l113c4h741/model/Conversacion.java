package cr.ac.ucr.c4l113c4h741.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** Datos propios de la aplicación. Los mensajes los guarda Spring AI (SPRING_AI_CHAT_MEMORY). */
@Entity
@Table(name = "conversacion")
public class Conversacion {

    public static final String TITULO_POR_DEFECTO = "Nueva conversación";

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 120)
    private String titulo = TITULO_POR_DEFECTO;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
