package modelo;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RolDAO {

   public static List listarRoles() {
        List<Rol> listaRoles = new ArrayList<>();

        String sql
                = "SELECT id_rol, nombre_rol, descripcion FROM roles";
        
        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("id_rol"));
                rol.setNombreRol(rs.getString("nombre_rol"));
                rol.setDescripcion(rs.getString("descripcion"));
                listaRoles.add(rol);
            }
        } catch (Exception e) {

            System.out.println(
                    "Error roles: "
                    + e.getMessage()
            );
        }
        return listaRoles;
    }

}
