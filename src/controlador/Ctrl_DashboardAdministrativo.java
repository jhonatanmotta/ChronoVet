package controlador;

import modelo.DashboardDAO;
import vista.dashboard.DashboardAdministrativo;
import java.util.Map;

public class Ctrl_DashboardAdministrativo {
    
    private DashboardAdministrativo vista;
    private DashboardDAO dashboardDAO;
    
    public Ctrl_DashboardAdministrativo(DashboardAdministrativo vista) {
        this.vista = vista;
        this.dashboardDAO = new DashboardDAO();
        cargarDatos();
    }
    
    private void cargarDatos() {
        Map<String, Object> datos = dashboardDAO.obtenerDatosAdministrativo();
        
        int citasPendientesTotales = (int) datos.getOrDefault("citasPendientesTotales", 0);
        int citasHoyPendientes = (int) datos.getOrDefault("citasHoyPendientes", 0);
        int citasHoyCompletadas = (int) datos.getOrDefault("citasHoyCompletadas", 0);
        
        // Actualizar la vista
        vista.actualizarDatos(citasPendientesTotales, citasHoyPendientes, citasHoyCompletadas);
    }
    
    // Método para refrescar los datos
    public void refrescarDatos() {
        cargarDatos();
    }
}
