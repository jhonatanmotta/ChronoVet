package modelo;

import conexion.Conexion;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EspecialidadDAO {
    
    public static List<Especialidad> listarEspecialidades() {

    List<Especialidad> listaEspecialidades =
            new ArrayList<>();

    Connection con = Conexion.conectar();

    String sql = """
                 SELECT id_especialidad,
                        nombre
                 FROM especialidades
                 ORDER BY nombre
                 """;

    try {

        PreparedStatement ps =
                con.prepareStatement(sql);

        ResultSet rs =
                ps.executeQuery();

        while (rs.next()) {

            Especialidad especialidad =
                    new Especialidad();

            especialidad.setIdEspecialidad(
                    rs.getInt("id_especialidad"));

            especialidad.setNombre(
                    rs.getString("nombre"));

            listaEspecialidades.add(especialidad);
        }

    } catch (Exception e) {

        System.out.println(
                "Error listar especialidades: "
                + e.getMessage());
    }

    return listaEspecialidades;
}
}
