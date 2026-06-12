package controlador;

import modelo.Usuario;
import utils.Sesion;
import vista.Panel_DashboardInicial;
import vista.dashboard.DashboardAdministrador;
import vista.dashboard.DashboardAdministrativo;
import vista.dashboard.DashboardCliente;
import vista.dashboard.DashboardVeterinario;

public class Ctrl_Dashboard {

    private Panel_DashboardInicial vista;
    private Usuario usuarioActual;
    private Ctrl_DashboardAdministrador ctrlAdmin;
    private Ctrl_DashboardAdministrativo ctrlAdminis;
    private Ctrl_DashboardCliente ctrlCliente;
    private Ctrl_DashboardVeterinario ctrlVeterinario;

    public Ctrl_Dashboard(Panel_DashboardInicial vista) {
        this.vista = vista;
        this.usuarioActual = Sesion.getInstancia().getUsuario();

        cargarDashboardSegunRol();
    }

    private void cargarDashboardSegunRol() {
        String rol = usuarioActual.getRol();

        if (vista.getPanelContenedor() == null) {
            System.err.println("ERROR: panelContenedor es null");
            return;
        }

        vista.getPanelContenedor().removeAll();
        vista.getPanelContenedor().setLayout(new java.awt.BorderLayout());
        vista.getPanelContenedor().setVisible(true);

        switch (rol) {
            case "Administrador":
                DashboardAdministrador dashboardAdmin = new DashboardAdministrador();
                ctrlAdmin = new Ctrl_DashboardAdministrador(dashboardAdmin);
                vista.getPanelContenedor().add(dashboardAdmin, java.awt.BorderLayout.CENTER);
                break;

            case "Administrativo":
                DashboardAdministrativo dashboardAdminis = new DashboardAdministrativo();
                ctrlAdminis = new Ctrl_DashboardAdministrativo(dashboardAdminis);
                vista.getPanelContenedor().add(dashboardAdminis, java.awt.BorderLayout.CENTER);
                break;

            case "Cliente":
                DashboardCliente dashboardCliente = new DashboardCliente();
                ctrlCliente = new Ctrl_DashboardCliente(dashboardCliente, usuarioActual.getIdUsuario());
                vista.getPanelContenedor().add(dashboardCliente, java.awt.BorderLayout.CENTER);
                break;

            case "Veterinario":
                DashboardVeterinario dashboardVeterinario = new DashboardVeterinario();
                ctrlVeterinario = new Ctrl_DashboardVeterinario(dashboardVeterinario, usuarioActual.getIdUsuario());
                vista.getPanelContenedor().add(dashboardVeterinario, java.awt.BorderLayout.CENTER);
                break;

            default:
                System.out.println("Rol no reconocido: " + rol);
                break;

        }

        vista.getPanelContenedor().revalidate();
        vista.getPanelContenedor().repaint();
    }

    public void refrescarDashboard() {
        if (ctrlAdmin != null) {
            ctrlAdmin.refrescarDatos();
        }
        if (ctrlAdminis != null) {
            ctrlAdminis.refrescarDatos();
        }
        if (ctrlCliente != null) {
            ctrlCliente.refrescarDatos();
        }
        if (ctrlVeterinario != null) {
            ctrlVeterinario.refrescarDatos();
        }
    }
}
