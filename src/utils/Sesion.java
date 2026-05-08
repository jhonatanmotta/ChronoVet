package utils;

import modelo.Usuario;

public class Sesion {
    private static Sesion instancia;

    private Usuario usuario;

    private Sesion() {
    }

    public static Sesion getInstancia() {

        if(instancia == null) {
            instancia = new Sesion();
        }

        return instancia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void cerrarSesion() {
        usuario = null;
    }
}
