
import conexion.Conexion;
import java.sql.*;

public class TestConexion {
   public static void main(String[] args) {

        Connection con = Conexion.conectar();

        if (con != null) {
            System.out.println("Conexión exitosa a PostgreSQL");
        } else {
            System.out.println("Error de conexión");
        }

    }
}
