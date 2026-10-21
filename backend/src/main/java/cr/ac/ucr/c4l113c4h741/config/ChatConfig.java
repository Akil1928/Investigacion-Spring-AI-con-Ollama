package cr.ac.ucr.c4l113c4h741.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class ChatConfig {

        /**
         * Memoria de ventana de mensajes (máximo 20). Se apoya en el repositorio JDBC
         * autoconfigurado, por lo que los mensajes se guardan en SQL Server.
         */
        @Bean
        ChatMemory chatMemory(ChatMemoryRepository repository,
                        @Value("${app.chat.max-messages:20}") int maxMessages) {
                return MessageWindowChatMemory.builder()
                                .chatMemoryRepository(repository)
                                .maxMessages(maxMessages)
                                .build();
        }

        /**
         * ChatClient construido sobre el ChatModel de Ollama (autoconfigurado).
         * El orden de los advisors importa: primero la memoria y luego el logger.
         */
        @Bean
        ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
                return builder
                                .defaultSystem("Eres un asistente que ayuda con Java y Spring. "
                                                + "Responde siempre en español, de forma clara y breve.")
                                .defaultAdvisors(
                                                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                                                new SimpleLoggerAdvisor())
                                .build();
        }
}
