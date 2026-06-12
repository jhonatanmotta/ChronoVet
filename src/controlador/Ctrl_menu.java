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
import vista.Panel_DashboardInicial;
import vista.Panel_GestionarCitas;
import vista.Panel_GestionarMascotas;
import vista.Panel_GestionarUsuarios;
import vista.Panel_RegistrarCitas;
import vista.Panel_RegistrarUsuario;

public class Ctrl_menu {

    private Menu menu;

    private CardLayout cardLayout;

    private Panel_RegistrarUsuario panelRegistrarUsuario;

    private Panel_GestionarUsuarios panelGestionarUsuarios;

    private Panel_GestionarMascotas panelGestionarMascotas;

    private Panel_GestionarCitas panelGestionarCitas;
    
    private Panel_DashboardInicial panelDashboard;
    
    private Ctrl_Dashboard ctrlDashboard;

    private Ctrl_usuario ctrlUsuario;

    private Ctrl_gestionUsuarios ctrlGestionUsuarios;

    private Ctrl_Mascota ctrlMascota;

    private Panel_RegistrarCitas panelRegistrarCitas;

    private Ctrl_Cita ctrlCita;
    
    private Ctrl_GestionCitas ctrlGestionCita;

    public Ctrl_menu(Menu menu) {

        this.menu = menu;

        iniciarPaneles();

        iniciarEventos();

        validarPermisosMenu();

        mostrarPanelInicial();
    }

    private void iniciarPaneles() {

        cardLayout = new CardLayout();

        // aqui debo instarciar los nuevos paneles que agregue
        menu.getPanelVistaPrincipal()
                .setLayout(cardLayout);

        panelRegistrarUsuario
                = new Panel_RegistrarUsuario();

        ctrlUsuario
                = new Ctrl_usuario(panelRegistrarUsuario);

        panelGestionarUsuarios
                = new Panel_GestionarUsuarios();

        ctrlGestionUsuarios
                = new Ctrl_gestionUsuarios(panelGestionarUsuarios);

        panelGestionarMascotas
                = new Panel_GestionarMascotas();

        ctrlMascota = new Ctrl_Mascota(
                panelGestionarMascotas,
                Sesion.getInstancia().getUsuario()
        );

        panelGestionarCitas
                = new Panel_GestionarCitas();

        ctrlGestionCita =
                new Ctrl_GestionCitas(panelGestionarCitas);
        
        panelRegistrarCitas
                = new Panel_RegistrarCitas();

        ctrlCita
                = new Ctrl_Cita(panelRegistrarCitas);

        panelDashboard
                = new Panel_DashboardInicial();
        
        ctrlDashboard = new Ctrl_Dashboard(panelDashboard);
        
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

        menu.getPanelVistaPrincipal()
                .add(panelRegistrarCitas,
                        "REGISTRAR_CITA");
        
        menu.getPanelVistaPrincipal()
                .add(panelDashboard,
                        "DASHBOARD");
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

        menu.getRegistrarCita()
                .addActionListener(
                        e -> mostrarRegistrarCita()
                );
        
        menu.getDashboardInicial()
                .addActionListener(
                        e -> mostrarDashboard()
                );
    }

    private void mostrarRegistrarUsuario() {
        cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "REGISTRAR_USUARIO"
        );
    }

    private void mostrarGestionUsuarios() {
        
        ctrlGestionUsuarios.recargarTabla();
        
        cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "GESTION_USUARIOS"
        );
    }

    private void mostrarGestionMascotas() {
        
        ctrlMascota.recargarDatos();
        
        cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "GESTION_MASCOTAS"
        );
    }

    private void mostrarRegistrarCita() {
        
        ctrlCita.cargarDatos();
        cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "REGISTRAR_CITA"
        );
    }

    private void mostrarGestionCitas() {
        
        ctrlGestionCita.recargarTabla();
        
        cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "GESTION_CITAS"
        );
    }
    
    private void mostrarDashboard() {
        
        if (ctrlDashboard != null) {
            ctrlDashboard.refrescarDashboard();
        }
        
        cardLayout.show(
                menu.getPanelVistaPrincipal(),
                "DASHBOARD"
        );
    }

    private void mostrarPanelInicial() {

        Usuario usuario = Sesion.getInstancia().getUsuario();

        String rol = usuario.getRol();

        switch (rol) {

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

        if (!rol.equals("Administrador")
                && !rol.equals("Administrativo")) {

            menu.getMenuUsuarios().setVisible(false);
        }
        if (rol.equals("Veterinario")) {

            menu.getRegistrarCita()
                    .setVisible(false);
        }
    }
}
