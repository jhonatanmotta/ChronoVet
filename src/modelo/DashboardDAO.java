package modelo;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import conexion.Conexion;

public class DashboardDAO {

    public Map<String, Object> obtenerDatosCliente(int idUsuario) {
        Map<String, Object> datos = new HashMap<>();
        
        String sql = """
            SELECT 
                COUNT(CASE WHEN c.estado = 'pendiente' THEN 1 END) as citas_pendientes,
                COUNT(CASE WHEN c.estado = 'completada' THEN 1 END) as citas_completadas,
                (SELECT COUNT(*) FROM mascotas WHERE id_dueno = ?) as total_mascotas
            FROM citas c
            WHERE c.id_usuario = ?
        """;
        
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    datos.put("citasPendientes", rs.getInt("citas_pendientes"));
                    datos.put("citasCompletadas", rs.getInt("citas_completadas"));
                    datos.put("totalMascotas", rs.getInt("total_mascotas"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return datos;
    }
    

    public Map<String, Object> obtenerDatosVeterinario(int idVeterinario) {
    Map<String, Object> datos = new HashMap<>();
    
    System.out.println("=== obtenerDatosVeterinario ===");
    System.out.println("idVeterinario recibido: " + idVeterinario);
    
    // Inicializar valores por defecto
    datos.put("citasPendientes", 0);
    datos.put("citasCompletadas", 0);
    datos.put("mascotasAtendidas", 0);
    
    String sql = """
        SELECT 
            COUNT(CASE WHEN estado = 'pendiente' THEN 1 END) as citas_pendientes,
            COUNT(CASE WHEN estado = 'completada' THEN 1 END) as citas_completadas,
            COUNT(DISTINCT CASE WHEN estado = 'completada' THEN id_mascota END) as mascotas_atendidas
        FROM citas
        WHERE id_veterinario = ?
    """;
    
    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        ps.setInt(1, idVeterinario);
        System.out.println("Consultando citas para id_veterinario: " + idVeterinario);
        
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int pendientes = rs.getInt("citas_pendientes");
                int completadas = rs.getInt("citas_completadas");
                int mascotas = rs.getInt("mascotas_atendidas");
                
                datos.put("citasPendientes", pendientes);
                datos.put("citasCompletadas", completadas);
                datos.put("mascotasAtendidas", mascotas);
                
                System.out.println("Resultados:");
                System.out.println("  Citas pendientes: " + pendientes);
                System.out.println("  Citas completadas: " + completadas);
                System.out.println("  Mascotas atendidas: " + mascotas);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error SQL: " + e.getMessage());
        e.printStackTrace();
    }
    
    return datos;
}
    
    public Map<String, Object> obtenerDatosAdministrativo() {
        Map<String, Object> datos = new HashMap<>();
        
        String sql = """
            SELECT 
                COUNT(CASE WHEN estado = 'pendiente' THEN 1 END) as citas_pendientes_totales,
                COUNT(CASE WHEN estado = 'pendiente' AND DATE(fecha_hora) = CURRENT_DATE THEN 1 END) as citas_hoy_pendientes,
                COUNT(CASE WHEN estado = 'completada' AND DATE(fecha_hora) = CURRENT_DATE THEN 1 END) as citas_hoy_completadas
            FROM citas
        """;
        
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                datos.put("citasPendientesTotales", rs.getInt("citas_pendientes_totales"));
                datos.put("citasHoyPendientes", rs.getInt("citas_hoy_pendientes"));
                datos.put("citasHoyCompletadas", rs.getInt("citas_hoy_completadas"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return datos;
    }
    
public Map<String, Object> obtenerDatosAdministrador() {
    Map<String, Object> datos = new HashMap<>();
    
    String sql = """
        SELECT 
            (SELECT COUNT(*) FROM usuarios) as total_usuarios,
            (SELECT COUNT(*) FROM usuarios WHERE id_rol = 4) as total_clientes,
            (SELECT COUNT(*) FROM citas WHERE estado = 'pendiente') as citas_pendientes
    """;
    
    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        
        if (rs.next()) {
            datos.put("totalUsuarios", rs.getInt("total_usuarios"));
            datos.put("totalClientes", rs.getInt("total_clientes"));
            datos.put("citasPendientes", rs.getInt("citas_pendientes"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return datos;
}
}
