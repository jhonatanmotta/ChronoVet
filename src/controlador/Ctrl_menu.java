package controlador;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import modelo.Usuario;
import utils.Sesion;
import vista.Menu;
import vista.Panel_GestionarCitas;
import vista.Panel_GestionarMascotas;
import vista.Panel_GestionarUsuarios;
import vista.Panel_RegistrarUsuario;

public class Ctrl_menu {

    private Menu menu;

    private CardLayout cardLayout;

    private Panel_RegistrarUsuario panelRegistrarUsuario;

    private Panel_GestionarUsuarios panelGestionarUsuarios;

    private Panel_GestionarMascotas panelGestionarMascotas;

    private Panel_GestionarCitas panelGestionarCitas;

    private Ctrl_usuario ctrlUsuario;

    public Ctrl_menu(Menu menu) {

        this.menu = menu;

        iniciarPaneles();

        iniciarEventos();
        
        validarPermisosMenu();
        
        mostrarPanelInicial();
    }

    private void iniciarPaneles() {

        cardLayout = new CardLayout();

        menu.getPanelVistaPrincipal()
                .setLayout(cardLayout);

        panelRegistrarUsuario
                = new Panel_RegistrarUsuario();

        ctrlUsuario = new Ctrl_usuario(panelRegistrarUsuario);

        panelGestionarUsuarios
                = new Panel_GestionarUsuarios();

        panelGestionarMascotas
                = new Panel_GestionarMascotas();

        panelGestionarCitas
                = new Panel_GestionarCitas();

        menu.getPanelVistaPrincipal()
                .add(panelRegistrarUsuario,
                        "REGISTRAR_USUARIO");

        menu.getPanelVistaPrincipal()
                .add(panelGestionarUsuarios,
                        "GESTION_USUARIOS");

        menu.getPanelVistaPrincipal()
                .add(panelGestionarMascotas,
                        "GESTION_MASCOTAS");

        menu.getPanelVistaPrincipal()
                .add(panelGestionarCitas,
                        "GESTION_CITAS");
    }

    private void iniciarEventos() {

        menu.getRegistrarUsuario()
                .addActionListener(e
                        -> mostrarRegistrarUsuario()
                );

        menu.getGestionarUsuario()
                .addActionListener(e
                        -> mostrarGestionUsuarios()
                );
        
        menu.getGestionarMascotas()
            .addActionListener(e
                    -> mostrarGestionMascotas()
            );

        menu.getGestionarCitas()
            .addActionListener(e
                    -> mostrarGestionCitas()
            );
    }

    private void mostrarRegistrarUsuario() {
        cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "REGISTRAR_USUARIO"
        );
    }

    private void mostrarGestionUsuarios() {
        cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "GESTION_USUARIOS"
        );
    }
    
    private void mostrarGestionMascotas() {
    cardLayout.show(
            menu.getPanelVistaPrincipal(),
            "GESTION_MASCOTAS"
    );
}
    
    private void mostrarGestionCitas() {
    cardLayout.show(
            menu.getPanelVistaPrincipal(),
            "GESTION_CITAS"
    );
}

    private void mostrarPanelInicial() {

    Usuario usuario = Sesion.getInstancia().getUsuario();

    String rol = usuario.getRol();

    switch(rol) {

        case "Administrador":

            cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "GESTION_USUARIOS"
            );

            break;

        case "Administrativo":

            cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "GESTION_USUARIOS"
            );

            break;

        case "Cliente":

            cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "GESTION_MASCOTAS"
            );

            break;

        case "Veterinario":

            cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "GESTION_CITAS"
            );

            break;
    }
}
    
    private void validarPermisosMenu() {

    Usuario usuario = Sesion.getInstancia().getUsuario();

    String rol = usuario.getRol();

    if(!rol.equals("Administrador")
       &&
       !rol.equals("Administrativo")) {

        menu.getMenuUsuarios().setVisible(false);
    }
}
}
