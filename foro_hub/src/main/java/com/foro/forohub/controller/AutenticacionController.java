package com.foro.forohub.controller;

import com.foro.forohub.domain.usuario.DatosAutenticacion;
import com.foro.forohub.domain.usuario.Usuario;
import com.foro.forohub.infra.security.TokenService;
import com.foro.forohub.infra.security.UserSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AutenticacionController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity autenticar(@RequestBody DatosAutenticacion datos) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(datos.email(), datos.contrasena());
        Authentication auth = authenticationManager.authenticate(authToken);

        //Usuario usuario = (Usuario) auth.getPrincipal();
        UserSpring userSpring = (UserSpring) auth.getPrincipal();

        Usuario usuario = userSpring.getUsuario();

        var jwtToken = tokenService.generarToken(usuario);
        return ResponseEntity.ok().body(new TokenDTO(jwtToken, "Bearer"));
    }

    // DTO para la respuesta
    record TokenDTO(String token, String type) {}
}
