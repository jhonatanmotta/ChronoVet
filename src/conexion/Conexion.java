package conexion;

import java.sql.*;

public class Conexion {
    private static final String URL =
        "jdbc:postgresql://localhost:5432/veterinaria_db";

    private static final String USER =
        "admin_veterinaria";

    private static final String PASSWORD =
        "admin1234";
    
     public static Connection conectar() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );
            System.out.println("Conectado");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return con;
    }
}
