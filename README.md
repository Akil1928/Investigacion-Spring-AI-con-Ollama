# Integración de Spring AI con Ollama — Parte práctica

**IF0009 Desarrollo de Software IV · Universidad de Costa Rica, Recinto de Paraíso**
**Integrantes (carnés):** C4L113 · C4H741· C5H753

Asistente de chat que se ejecuta **100 % en local**: Angular (frontend) → Spring Boot + Spring AI (backend) → Ollama (modelo) y SQL Server (persistencia).

```
Angular 22 ──HTTP/JSON──► Spring Boot 4 ──► Ollama (llama3.2, :11434)
 (:4200)                   (:8080)     └──► SQL Server  (Spring_AI_DataBase)
                                              ├─ conversacion          (JPA)
                                              └─ SPRING_AI_CHAT_MEMORY (Spring AI, JdbcChatMemoryRepository)
```

## Requisitos
| Herramienta | Versión |
|---|---|
| JDK | 17 o superior (21 recomendado) |
| Node.js / npm / Angular CLI | 24.21.0 / 11.19.0 / 22.1.8 |
| SQL Server + SSMS | SSMS 21 |
| Ollama | https://ollama.com (instalado y en ejecución) |
| VS Code | Extensiones: *Extension Pack for Java*, *Spring Boot Extension Pack*, *Angular Language Service* (se sugieren al abrir la carpeta) |

> Maven no hace falta instalarlo: el proyecto incluye Maven Wrapper (`mvnw.cmd`) y lo descarga la primera vez (requiere internet).

## 1. Ollama
```powershell
ollama pull llama3.2
ollama list          # debe aparecer llama3.2
```
Ollama queda escuchando en `http://localhost:11434`. El modelo se cambia en `backend/src/main/resources/application.properties` (`spring.ai.ollama.chat.options.model`).

## 2. Base de datos (SSMS 21)
1. Conéctese a su instancia de SQL Server.
2. Verifique que exista la base **`Spring_AI_DataBase`** y que el usuario **`Lenguajes`** tenga acceso (`database/01_verificar_base_de_datos.sql`).
3. La aplicación crea sola las tablas `conversacion` y `SPRING_AI_CHAT_MEMORY`.
4. **Conexión TCP:** en *SQL Server Configuration Manager* → *Protocolos de SQL Server* → habilite **TCP/IP** (puerto 1433) y reinicie el servicio. Si usa una instancia con nombre (p. ej. `SQLEXPRESS`), cambie la URL en `application.properties` a  
   `jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=Spring_AI_DataBase;encrypt=true;trustServerCertificate=true`
5. El usuario debe poder iniciar sesión con **autenticación de SQL Server** (modo mixto).

## 3. Backend (VS Code)
Abra la carpeta raíz en VS Code y, en una terminal:
Si usa la tarea de VS Code, le pedirá la contraseña
```powershell
cd backend
$env:DB_PASSWORD = "su_clave" 
.\mvnw.cmd spring-boot:run
```
(o use la tarea *Terminal → Run Task → Backend: iniciar Spring Boot*). La API queda en `http://localhost:8080`.

## 4. Frontend (VS Code)
En otra terminal:
```powershell
cd frontend
npm install
npm start
```
Abra `http://localhost:4200`.

## API REST
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/conversaciones` | Crea una conversación (`{"titulo": "..."}`) |
| GET | `/api/conversaciones` | Lista las conversaciones |
| GET | `/api/conversaciones/{id}/mensajes` | Historial de la conversación |
| POST | `/api/conversaciones/{id}/mensajes` | Envía un mensaje (`{"texto": "..."}`) y devuelve la respuesta y el tiempo en ms |
| DELETE | `/api/conversaciones/{id}` | Elimina la conversación y sus mensajes |

## Prueba rápida sin frontend
En PowerShell (use texto sin tildes en estas pruebas):
```powershell
$c  = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/conversaciones -ContentType "application/json" -Body '{}'
$id = $c.id
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/conversaciones/$id/mensajes" -ContentType "application/json" -Body '{"texto":"Mi nombre es Ana"}'
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/conversaciones/$id/mensajes" -ContentType "application/json" -Body '{"texto":"Como me llamo?"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/conversaciones/$id/mensajes"
```
La segunda pregunta comprueba que el modelo usa el historial (memoria de conversación). Para la sección 4.6 del documento, reinicie el backend y consulte de nuevo el historial: debe seguir allí. Use `database/02_consultas_de_prueba.sql` para ver los datos en SSMS.

## Problemas frecuentes
| Síntoma | Causa / solución |
|---|---|
| `Login failed for user 'Lenguajes'` | Usuario o contraseña incorrectos, o no está habilitado el modo mixto / permisos sobre la base. |
| `The TCP/IP connection to the host localhost, port 1433 has failed` | Habilitar TCP/IP (paso 2.4) o usar `instanceName`. |
| Error 503 "No se pudo obtener respuesta de Ollama" | Ollama no está corriendo o falta `ollama pull llama3.2`. |
| Error al reiniciar por tabla `SPRING_AI_CHAT_MEMORY` ya existente | Poner `spring.ai.chat.memory.repository.jdbc.initialize-schema=never`. |
| `Could not resolve ... spring-boot-starter-parent:4.1.1` | Cambiar la versión del parent en `pom.xml` a `4.0.1` (Spring AI 2.0.1 admite Boot 4.0.x y 4.1.x). |
| Primera respuesta muy lenta | Normal: Ollama carga el modelo en memoria; las siguientes son más rápidas (`keep_alive` = 5 min). |
| Error de CORS en el navegador | El frontend debe correr en `http://localhost:4200` (propiedad `app.cors.allowed-origin`). |

> Seguridad: la contraseña de la base NO se guarda en el repositorio. Se entrega por la variable de entorno `DB_PASSWORD` (o la pide la tarea de VS Code). Opcionalmente `DB_URL` y `DB_USER`.
