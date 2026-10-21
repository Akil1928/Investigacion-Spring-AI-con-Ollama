import { Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ChatService } from './chat.service';
import { Conversacion, Mensaje } from './models';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private readonly chat = inject(ChatService);
  private readonly caja = viewChild<ElementRef<HTMLDivElement>>('caja');

  readonly autores = 'C4L113 / C4H741 / C5H753';

  readonly conversaciones = signal<Conversacion[]>([]);
  readonly seleccionada = signal<Conversacion | null>(null);
  readonly mensajes = signal<Mensaje[]>([]);
  readonly texto = signal('');
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly ultimoTiempo = signal<number | null>(null);

  ngOnInit(): void {
    this.cargarConversaciones();
  }

  cargarConversaciones(): void {
    this.chat.listar().subscribe({
      next: (lista) => this.conversaciones.set(lista),
      error: (e) => this.mostrarError(e),
    });
  }

  nueva(): void {
    this.chat.crear('Nueva conversación').subscribe({
      next: (c) => {
        this.conversaciones.update((l) => [c, ...l]);
        this.seleccionar(c);
      },
      error: (e) => this.mostrarError(e),
    });
  }

  seleccionar(c: Conversacion): void {
    this.error.set(null);
    this.ultimoTiempo.set(null);
    this.seleccionada.set(c);
    this.chat.historial(c.id).subscribe({
      next: (m) => {
        this.mensajes.set(m);
        this.bajar();
      },
      error: (e) => this.mostrarError(e),
    });
  }

  enviar(): void {
    const c = this.seleccionada();
    const texto = this.texto().trim();
    if (!c || !texto || this.cargando()) {
      return;
    }
    this.error.set(null);
    this.mensajes.update((m) => [...m, { rol: 'USER', texto }]);
    this.texto.set('');
    this.cargando.set(true);
    this.bajar();

    this.chat.enviar(c.id, texto).subscribe({
      next: (r) => {
        this.mensajes.update((m) => [...m, { rol: 'ASSISTANT', texto: r.respuesta }]);
        this.ultimoTiempo.set(r.milisegundos);
        this.cargando.set(false);
        // Actualiza el título (la primera pregunta se usa como título)
        this.conversaciones.update((l) => l.map((x) => (x.id === c.id ? { ...x, titulo: r.titulo } : x)));
        this.seleccionada.update((s) => (s ? { ...s, titulo: r.titulo } : s));
        this.bajar();
      },
      error: (e) => {
        this.cargando.set(false);
        this.mostrarError(e);
      },
    });
  }

  eliminar(c: Conversacion, evento: Event): void {
    evento.stopPropagation();
    if (!confirm(`¿Eliminar la conversación "${c.titulo}"?`)) {
      return;
    }
    this.chat.eliminar(c.id).subscribe({
      next: () => {
        this.conversaciones.update((l) => l.filter((x) => x.id !== c.id));
        if (this.seleccionada()?.id === c.id) {
          this.seleccionada.set(null);
          this.mensajes.set([]);
        }
      },
      error: (e) => this.mostrarError(e),
    });
  }

  alEscribir(evento: Event): void {
    this.texto.set((evento.target as HTMLTextAreaElement).value);
  }

  alPresionarEnter(evento: Event): void {
    const e = evento as KeyboardEvent;
    if (!e.shiftKey) {
      e.preventDefault();
      this.enviar();
    }
  }

  private mostrarError(e: unknown): void {
    if (e instanceof HttpErrorResponse) {
      this.error.set(
        e.status === 0
          ? 'No se pudo conectar con el backend (http://localhost:8080). ¿Está corriendo?'
          : (e.error?.message ?? `Error ${e.status}`)
      );
    } else {
      this.error.set('Ocurrió un error inesperado.');
    }
  }

  private bajar(): void {
    setTimeout(() => {
      const el = this.caja()?.nativeElement;
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    });
  }
}
