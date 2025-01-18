package com.foro.forohub.infra.security;

import com.foro.forohub.domain.usuario.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserSpring implements UserDetails {

    private final Usuario usuario;

    public UserSpring(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Si no manejas roles/permisos específicos, puedes retornar una lista vacía o algo básico:
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return usuario.getContrasena();
    }

    @Override
    public String getUsername() {
        return usuario.getCorreoElectronico();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Por ejemplo, si tienes un campo usuario.getActivo()...
        return Boolean.TRUE.equals(usuario.getActivo());
    }

    public Usuario getUsuario() {
        return this.usuario;
    }
}
