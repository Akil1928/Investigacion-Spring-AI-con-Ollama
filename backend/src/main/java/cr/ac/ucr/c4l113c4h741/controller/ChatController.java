package cr.ac.ucr.c4l113c4h741.controller;

import cr.ac.ucr.c4l113c4h741.dto.CrearConversacionRequest;
import cr.ac.ucr.c4l113c4h741.dto.MensajeDto;
import cr.ac.ucr.c4l113c4h741.dto.MensajeRequest;
import cr.ac.ucr.c4l113c4h741.dto.RespuestaDto;
import cr.ac.ucr.c4l113c4h741.model.Conversacion;
import cr.ac.ucr.c4l113c4h741.repository.ConversacionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversaciones")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ConversacionRepository conversaciones;

    public ChatController(ChatClient chatClient, ChatMemory chatMemory,
                          ConversacionRepository conversaciones) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.conversaciones = conversaciones;
    }

    @PostMapping
    public Conversacion crear(@RequestBody(required = false) CrearConversacionRequest body) {
        Conversacion c = new Conversacion();
        if (body != null && body.titulo() != null && !body.titulo().isBlank()) {
            c.setTitulo(recortar(body.titulo().trim(), 120));
        }
        return conversaciones.save(c);
    }

    @GetMapping
    public List<Conversacion> listar() {
        return conversaciones.findAllByOrderByFechaCreacionDesc();
    }

    @GetMapping("/{id}/mensajes")
    public List<MensajeDto> historial(@PathVariable UUID id) {
        buscar(id);
        return chatMemory.get(id.toString()).stream()
                .map(m -> new MensajeDto(m.getMessageType().name(), m.getText()))
                .toList();
    }

    @PostMapping("/{id}/mensajes")
    public RespuestaDto enviar(@PathVariable UUID id, @RequestBody MensajeRequest body) {
        Conversacion conversacion = buscar(id);
        if (body == null || body.texto() == null || body.texto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje no puede estar vacío");
        }

        long inicio = System.currentTimeMillis();
        String respuesta;
        try {
            // call() solo define el modo; el modelo se ejecuta al pedir content()
            respuesta = chatClient.prompt()
                    .user(body.texto())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id.toString()))
                    .call()
                    .content();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo obtener respuesta de Ollama. ¿Está corriendo y descargado el modelo? ("
                            + e.getClass().getSimpleName() + ")");
        }
        long ms = System.currentTimeMillis() - inicio;

        // La primera pregunta se usa como título de la conversación
        if (Conversacion.TITULO_POR_DEFECTO.equals(conversacion.getTitulo())) {
            conversacion.setTitulo(recortar(body.texto().trim(), 40));
            conversaciones.save(conversacion);
        }
        return new RespuestaDto(respuesta, conversacion.getTitulo(), ms);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable UUID id) {
        buscar(id);
        chatMemory.clear(id.toString());
        conversaciones.deleteById(id);
    }

    private Conversacion buscar(UUID id) {
        return conversaciones.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
    }

    private static String recortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
