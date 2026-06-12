package modelo;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.List;
import utils.RespuestaCita;

/**
 *
 * @author jhona
 */
public class CitaDAO {

public RespuestaCita registrar(Cita cita) {

    String sql = "{ call sp_registrar_cita(?, ?, ?, ?, ?, ?, ?) }";

    RespuestaCita respuesta = new RespuestaCita();

    try (Connection con = Conexion.conectar();
         CallableStatement cs = con.prepareCall(sql)) {

        cs.setInt(1, cita.getIdUsuario());
        cs.setInt(2, cita.getIdMascota());
        cs.setInt(3, cita.getIdEspecialidad());
        cs.setTimestamp(4, cita.getFechaHora());
        cs.setString(5, cita.getMotivoConsulta());

        cs.registerOutParameter(6, java.sql.Types.INTEGER);
        cs.registerOutParameter(7, java.sql.Types.VARCHAR);

        cs.execute();

        respuesta.setIdCita(cs.getInt(6));
        respuesta.setMensaje(cs.getString(7));

    } catch (Exception e) {
        respuesta.setIdCita(-1);
        respuesta.setMensaje("Error: " + e.getMessage());
    }

    return respuesta;
}

public List<Cita> listarTodas() {

    List<Cita> lista = new ArrayList<>();

    String sql = """
        SELECT
            c.id_cita,
            m.nombre AS mascota,
            u.nombre || ' ' || u.apellido AS cliente,
            uv.nombre || ' ' || uv.apellido AS veterinario,
            e.nombre AS especialidad,
            c.fecha_hora,
            c.estado
        FROM citas c
        INNER JOIN mascotas m
            ON c.id_mascota = m.id_mascota
        INNER JOIN usuarios u
            ON c.id_usuario = u.id_usuario
        INNER JOIN veterinarios v
            ON c.id_veterinario = v.id_veterinario
        INNER JOIN usuarios uv
            ON v.id_usuario = uv.id_usuario
        INNER JOIN especialidades e
            ON c.id_especialidad = e.id_especialidad
        ORDER BY c.fecha_hora
        """;

    try (
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            Cita cita = new Cita();

            cita.setIdCita(rs.getInt("id_cita"));
            cita.setNombreMascota(rs.getString("mascota"));
            cita.setNombreCliente(rs.getString("cliente"));
            cita.setNombreVeterinario(rs.getString("veterinario"));
            cita.setNombreEspecialidad(rs.getString("especialidad"));
            cita.setFechaHora(rs.getTimestamp("fecha_hora"));
            cita.setEstado(rs.getString("estado"));

            lista.add(cita);
        }

    } catch (Exception e) {

        System.out.println(
                "Error listar citas: "
                + e.getMessage());
    }

    return lista;
}

public List<Cita> listarPorCliente(int idUsuario) {

    List<Cita> lista = new ArrayList<>();

    String sql = """
        SELECT
            c.id_cita,
            m.nombre AS mascota,
            u.nombre || ' ' || u.apellido AS cliente,
            uv.nombre || ' ' || uv.apellido AS veterinario,
            e.nombre AS especialidad,
            c.fecha_hora,
            c.estado
        FROM citas c
        INNER JOIN mascotas m
            ON c.id_mascota = m.id_mascota
        INNER JOIN usuarios u
            ON c.id_usuario = u.id_usuario
        INNER JOIN veterinarios v
            ON c.id_veterinario = v.id_veterinario
        INNER JOIN usuarios uv
            ON v.id_usuario = uv.id_usuario
        INNER JOIN especialidades e
            ON c.id_especialidad = e.id_especialidad
        WHERE c.id_usuario = ?
        ORDER BY c.fecha_hora
        """;

    try (
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idUsuario);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Cita cita = new Cita();

                cita.setIdCita(rs.getInt("id_cita"));
                cita.setNombreMascota(rs.getString("mascota"));
                cita.setNombreCliente(rs.getString("cliente"));
                cita.setNombreVeterinario(rs.getString("veterinario"));
                cita.setNombreEspecialidad(rs.getString("especialidad"));
                cita.setFechaHora(rs.getTimestamp("fecha_hora"));
                cita.setEstado(rs.getString("estado"));

                lista.add(cita);
            }
        }

    } catch (Exception e) {

        System.out.println(
                "Error listar citas cliente: "
                + e.getMessage());
    }

    return lista;
}

public List<Cita> listarPorVeterinario(int idVeterinario) {

    List<Cita> lista = new ArrayList<>();

    String sql = """
        SELECT
            c.id_cita,
            m.nombre AS mascota,
            u.nombre || ' ' || u.apellido AS cliente,
            uv.nombre || ' ' || uv.apellido AS veterinario,
            e.nombre AS especialidad,
            c.fecha_hora,
            c.estado
        FROM citas c
        INNER JOIN mascotas m
            ON c.id_mascota = m.id_mascota
        INNER JOIN usuarios u
            ON c.id_usuario = u.id_usuario
        INNER JOIN veterinarios v
            ON c.id_veterinario = v.id_veterinario
        INNER JOIN usuarios uv
            ON v.id_usuario = uv.id_usuario
        INNER JOIN especialidades e
            ON c.id_especialidad = e.id_especialidad
        WHERE c.id_veterinario = ?
        ORDER BY c.fecha_hora
        """;

    try (
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idVeterinario);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Cita cita = new Cita();

                cita.setIdCita(
                        rs.getInt("id_cita"));

                cita.setNombreMascota(
                        rs.getString("mascota"));

                cita.setNombreCliente(
                        rs.getString("cliente"));

                cita.setNombreVeterinario(
                        rs.getString("veterinario"));

                cita.setNombreEspecialidad(
                        rs.getString("especialidad"));

                cita.setFechaHora(
                        rs.getTimestamp("fecha_hora"));

                cita.setEstado(
                        rs.getString("estado"));

                lista.add(cita);
            }
        }

    } catch (Exception e) {

        System.out.println(
                "Error listar citas por veterinario: "
                + e.getMessage());
    }

    return lista;
}

public Cita buscarPorId(int idCita) {

    Cita cita = null;

    String sql = """
        SELECT
            c.id_cita,
            c.id_mascota,
            c.id_usuario,
            c.id_veterinario,
            c.id_especialidad,
            c.fecha_hora,
            c.estado,
            c.motivo_consulta,
            m.nombre AS mascota,
            u.nombre || ' ' || u.apellido AS cliente,
            uv.nombre || ' ' || uv.apellido AS veterinario,
            e.nombre AS especialidad
        FROM citas c
        INNER JOIN mascotas m
            ON c.id_mascota = m.id_mascota
        INNER JOIN usuarios u
            ON c.id_usuario = u.id_usuario
        INNER JOIN veterinarios v
            ON c.id_veterinario = v.id_veterinario
        INNER JOIN usuarios uv
            ON v.id_usuario = uv.id_usuario
        INNER JOIN especialidades e
            ON c.id_especialidad = e.id_especialidad
        WHERE c.id_cita = ?
        """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idCita);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                cita = new Cita();

                cita.setIdCita(rs.getInt("id_cita"));
                cita.setIdMascota(rs.getInt("id_mascota"));
                cita.setIdUsuario(rs.getInt("id_usuario"));
                cita.setIdVeterinario(rs.getInt("id_veterinario"));
                cita.setIdEspecialidad(rs.getInt("id_especialidad"));
                cita.setFechaHora(rs.getTimestamp("fecha_hora"));
                cita.setEstado(rs.getString("estado"));
                cita.setMotivoConsulta(rs.getString("motivo_consulta"));

                cita.setNombreMascota(rs.getString("mascota"));
                cita.setNombreCliente(rs.getString("cliente"));
                cita.setNombreVeterinario(rs.getString("veterinario"));
                cita.setNombreEspecialidad(rs.getString("especialidad"));
            }
        }

    } catch (Exception e) {

        System.out.println("Error buscar cita: " + e.getMessage());
    }

    return cita;
}

public boolean cancelarCita(int idCita) {

    String sql = """
        UPDATE citas
        SET estado = 'cancelada'
        WHERE id_cita = ?
        AND estado = 'pendiente'
        """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idCita);

        return ps.executeUpdate() > 0;

    } catch (Exception e) {

        System.out.println("Error cancelar cita: " + e.getMessage());
        return false;
    }
}

}
