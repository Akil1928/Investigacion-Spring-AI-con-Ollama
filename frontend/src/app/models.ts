export interface Conversacion {
  id: string;
  titulo: string;
  fechaCreacion: string;
}

export interface Mensaje {
  rol: string;   // USER | ASSISTANT | SYSTEM
  texto: string;
}

export interface Respuesta {
  respuesta: string;
  titulo: string;
  milisegundos: number;
}
