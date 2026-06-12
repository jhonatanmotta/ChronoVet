
package modelo;

import java.sql.*;
import conexion.Conexion;

public class ConsultaDAO {
    
    public Object[] registrarConsulta(Consulta consulta) {
        Object[] resultado = new Object[2];
        String sql = "{ call sp_registrar_consulta(?, ?, ?, ?, ?, ?, ?) }";

        try (Connection con = Conexion.conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            // Parámetros de entrada
            cs.setInt(1, consulta.getIdCita());
            cs.setInt(2, consulta.getIdVeterinario());
            cs.setString(3, consulta.getDiagnostico());
            cs.setString(4, consulta.getTratamiento());
            cs.setString(5, consulta.getObservaciones());

            // Parámetros de salida
            cs.registerOutParameter(6, Types.INTEGER); 
            cs.registerOutParameter(7, Types.VARCHAR); 

            cs.execute();

            resultado[0] = cs.getInt(6);  // id_consulta
            resultado[1] = cs.getString(7); // mensaje

        } catch (SQLException e) {
            resultado[0] = null;
            resultado[1] = "Error en BD: " + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            resultado[0] = null;
            resultado[1] = "Error general: " + e.getMessage();
        }
        return resultado;
    }
}
