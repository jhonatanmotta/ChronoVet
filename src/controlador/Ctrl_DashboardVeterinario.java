package controlador;
import modelo.DashboardDAO;
import modelo.VeterinarioDAO;
import vista.dashboard.DashboardVeterinario;
import java.util.Map;

public class Ctrl_DashboardVeterinario {
    private DashboardVeterinario vista;
    private DashboardDAO dashboardDAO;
    private VeterinarioDAO veterinarioDAO;
    private int idVeterinario;
    
    public Ctrl_DashboardVeterinario(DashboardVeterinario vista, int idUsuario) {
        this.vista = vista;
        this.dashboardDAO = new DashboardDAO();
        this.veterinarioDAO = new VeterinarioDAO();
        
            System.out.println("=== Ctrl_DashboardVeterinario ===");
    System.out.println("ID Usuario recibido en constructor: " + idUsuario);
        
        // Obtener id_veterinario a partir del id_usuario
        this.idVeterinario = veterinarioDAO.obtenerIdVeterinario(idUsuario);
        
        System.out.println("ID Veterinario encontrado: " + idVeterinario);
        cargarDatos();
    }
    
    private void cargarDatos() {
        Map<String, Object> datos = dashboardDAO.obtenerDatosVeterinario(idVeterinario);
        
        int citasPendientes = (int) datos.getOrDefault("citasPendientes", 0);
        int citasCompletadas = (int) datos.getOrDefault("citasCompletadas", 0);
        int mascotasAtendidas = (int) datos.getOrDefault("mascotasAtendidas", 0);
        
        // Actualizar la vista
        vista.actualizarDatos(citasPendientes, citasCompletadas, mascotasAtendidas);
    }
    
    // Método para refrescar los datos
    public void refrescarDatos() {
        cargarDatos();
    }
}
