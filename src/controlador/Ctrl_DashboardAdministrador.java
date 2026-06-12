package controlador;

import modelo.DashboardDAO;
import vista.dashboard.DashboardAdministrador;
import java.util.Map;

public class Ctrl_DashboardAdministrador {
    
    private DashboardAdministrador vista;
    private DashboardDAO dashboardDAO;
    
    public Ctrl_DashboardAdministrador(DashboardAdministrador vista) {
        this.vista = vista;
        this.dashboardDAO = new DashboardDAO();
        cargarDatos();
    }
    
    private void cargarDatos() {
        Map<String, Object> datos = dashboardDAO.obtenerDatosAdministrador();
        
        int totalUsuarios = (int) datos.getOrDefault("totalUsuarios", 0);
        int totalClientes = (int) datos.getOrDefault("totalClientes", 0);
        int citasPendientes = (int) datos.getOrDefault("citasPendientes", 0);
        
        // Actualizar la vista
        vista.actualizarDatos(totalUsuarios, totalClientes, citasPendientes);
    }
    
    // Método para refrescar los datos
    public void refrescarDatos() {
        cargarDatos();
    }
}
