package controlador;
import modelo.DashboardDAO;
import vista.dashboard.DashboardCliente;
import java.util.Map;

public class Ctrl_DashboardCliente {
     private DashboardCliente vista;
    private DashboardDAO dashboardDAO;
    private int idUsuario;
    
    public Ctrl_DashboardCliente(DashboardCliente vista, int idUsuario) {
        this.vista = vista;
        this.dashboardDAO = new DashboardDAO();
        this.idUsuario = idUsuario;
        cargarDatos();
    }
    
    private void cargarDatos() {
        Map<String, Object> datos = dashboardDAO.obtenerDatosCliente(idUsuario);
        
        int citasPendientes = (int) datos.getOrDefault("citasPendientes", 0);
        int citasCompletadas = (int) datos.getOrDefault("citasCompletadas", 0);
        int totalMascotas = (int) datos.getOrDefault("totalMascotas", 0);
        
        // Actualizar la vista
        vista.actualizarDatos(citasPendientes, citasCompletadas, totalMascotas);
    }
    
    // Método para refrescar los datos
    public void refrescarDatos() {
        cargarDatos();
    }
}
