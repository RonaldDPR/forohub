package com.foro.forohub.controller;

import com.foro.forohub.domain.topico.*;
import com.foro.forohub.domain.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/topicos")
public class TopicoController {

    @Autowired
    private TopicoRepository topicoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<TopicoDTO> listar() {
        // Solo tópicos activos
        return topicoRepository.findAll().stream()
                .filter(Topico::getActivo)
                .map(TopicoDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public TopicoDTO detalle(@PathVariable Long id) {
        var topico = topicoRepository.findById(id)
                // Si el ID no existe en BD => 404 Not Found
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tópico no encontrado (ID inválido)"
                ));

        // Si el tópico está inactivo => 410
        if (!topico.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "El tópico ya ha sido eliminado."
            );
        }

        // Si llega aquí, está activo. Devolvemos el DTO
        return new TopicoDTO(topico);
    }


    @PostMapping
    @Transactional
    public TopicoDTO registrar(@RequestBody @Valid DatosRegistroTopico datos) {
        // Verificar duplicados
        boolean existe = topicoRepository.existsByTituloAndMensaje(datos.titulo(), datos.mensaje());
        if (existe) {
            throw new RuntimeException("Tópico duplicado");
        }
        var usuario = usuarioRepository.findById(datos.idAutor())
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        var topico = new Topico(
                datos.titulo(),
                datos.mensaje(),
                LocalDateTime.now(),
                "ABIERTO",
                usuario
        );
        topicoRepository.save(topico);
        return new TopicoDTO(topico);
    }

    @PutMapping("/{id}")
    @Transactional
    public TopicoDTO actualizar(@PathVariable Long id, @RequestBody @Valid DatosRegistroTopico datos) {
        var topico = topicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tópico no encontrado (ID inválido)"
                ));

        // Impedimos actualizar si está inactivo
        if (!topico.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "El tópico ya ha sido eliminado, no se puede actualizar."
            );
        }

        // Actualizamos los campos
        topico.setTitulo(datos.titulo());
        topico.setMensaje(datos.mensaje());

        // Retornas el DTO con la nueva info
        return new TopicoDTO(topico);
    }


    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        var topico = topicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tópico no encontrado (ID inválido)"
                ));

        // Si ya está inactivo, arrojamos un mensaje distinto
        if (!topico.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "El tópico ya fue eliminado antes."
            );
        }

        // Eliminación lógica (activo=false)
        topico.desactivar();

        // Retornar un mensaje JSON de confirmación
        return ResponseEntity.ok(
                Map.of("mensaje", "Tópico eliminado correctamente")
        );
    }

}
