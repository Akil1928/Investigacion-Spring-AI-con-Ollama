import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Conversacion, Mensaje, Respuesta } from './models';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly http = inject(HttpClient);
  private readonly api = 'http://localhost:8080/api/conversaciones';

  listar() {
    return this.http.get<Conversacion[]>(this.api);
  }

  crear(titulo: string) {
    return this.http.post<Conversacion>(this.api, { titulo });
  }

  historial(id: string) {
    return this.http.get<Mensaje[]>(`${this.api}/${id}/mensajes`);
  }

  enviar(id: string, texto: string) {
    return this.http.post<Respuesta>(`${this.api}/${id}/mensajes`, { texto });
  }

  eliminar(id: string) {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
