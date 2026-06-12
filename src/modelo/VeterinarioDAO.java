package modelo;

import conexion.Conexion;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;

public class VeterinarioDAO {
    public int obtenerIdVeterinario(int idUsuario) {

    String sql = """
        SELECT id_veterinario
        FROM veterinarios
        WHERE id_usuario = ?
        """;

    try (
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idUsuario);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("id_veterinario");
            }
        }

    } catch (Exception e) {
        System.out.println(e.getMessage());
    }

    return 0;
}
}
