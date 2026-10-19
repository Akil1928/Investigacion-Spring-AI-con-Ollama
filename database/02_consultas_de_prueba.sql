-- Ejecutar DESPUÉS de iniciar el backend y conversar un poco con el modelo.
USE Spring_AI_DataBase;
GO

-- Conversaciones (tabla gestionada por JPA)
SELECT id, titulo, fechaCreacion FROM conversacion ORDER BY fechaCreacion DESC;

-- Mensajes (tabla gestionada por Spring AI - JdbcChatMemoryRepository)
SELECT * FROM SPRING_AI_CHAT_MEMORY;

-- Cantidad de mensajes por conversación
SELECT conversation_id, COUNT(*) AS mensajes
FROM SPRING_AI_CHAT_MEMORY
GROUP BY conversation_id;
GO
