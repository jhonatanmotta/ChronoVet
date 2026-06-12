package modelo;

import conexion.Conexion;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MascotaDAO {

    public List<Mascota> listarTodas() {

        List<Mascota> lista = new ArrayList<>();

        String sql = """
    SELECT
        m.*,
        u.nombre || ' ' || u.apellido AS propietario
    FROM mascotas m
    INNER JOIN usuarios u
        ON m.id_dueno = u.id_usuario
    ORDER BY m.nombre""";

        try (
                Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Mascota m = new Mascota();

                m.setIdMascota(rs.getInt("id_mascota"));
                m.setNombre(rs.getString("nombre"));
                m.setEspecie(rs.getString("especie"));
                m.setRaza(rs.getString("raza"));
                //m.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                m.setFechaNacimiento(new java.util.Date(rs.getDate("fecha_nacimiento").getTime()));
                m.setIdDueno(rs.getInt("id_dueno"));
                m.setNombreDueno(rs.getString("propietario")
                );

                lista.add(m);
            }

        } catch (Exception e) {
            System.out.println("Error listar todas las mascotas: " + e.getMessage());
        }

        return lista;
    }

    public List<Mascota> listarPorDueno(int idDueno) {

        List<Mascota> lista = new ArrayList<>();

        String sql = "SELECT * FROM mascotas WHERE id_dueno = ?";

        try (
                Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql);) {

            ps.setInt(1, idDueno);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Mascota m = new Mascota();

                    m.setIdMascota(rs.getInt("id_mascota"));
                    m.setNombre(rs.getString("nombre"));
                    m.setEspecie(rs.getString("especie"));
                    m.setRaza(rs.getString("raza"));
                    m.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                    m.setIdDueno(rs.getInt("id_dueno"));

                    lista.add(m);
                }
            }

        } catch (Exception e) {
            System.out.println("Error listar mascotas por dueño: " + e.getMessage());
        }

        return lista;
    }

    public boolean registrar(Mascota mascota) {

        String sql = """
        INSERT INTO mascotas
        (
            nombre,
            especie,
            raza,
            fecha_nacimiento,
            id_dueno
        )
        VALUES (?, ?, ?, ?, ?)
        """;

        try (
                Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mascota.getNombre());

            ps.setString(2, mascota.getEspecie());

            ps.setString(3, mascota.getRaza());

            ps.setDate(
                    4,
                    new java.sql.Date(
                            mascota.getFechaNacimiento().getTime()
                    )
            );

            ps.setInt(5, mascota.getIdDueno());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            System.out.println(
                    "Error registrar mascota: "
                    + e.getMessage()
            );

            return false;
        }
    }

    public boolean actualizar(Mascota mascota) {

        String sql = """
        UPDATE mascotas
        SET nombre = ?,
            especie = ?,
            raza = ?,
            fecha_nacimiento = ?,
            id_dueno = ?
        WHERE id_mascota = ?
        """;

        try (
                Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mascota.getNombre());

            ps.setString(2, mascota.getEspecie());

            ps.setString(3, mascota.getRaza());

            ps.setDate(
                    4,
                    new java.sql.Date(
                            mascota.getFechaNacimiento().getTime()
                    )
            );

            ps.setInt(5, mascota.getIdDueno());

            ps.setInt(6, mascota.getIdMascota());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            System.out.println(
                    "Error actualizar mascota: "
                    + e.getMessage()
            );

            return false;
        }
    }

    public Mascota buscarPorId(int idMascota) {

        Mascota mascota = null;

        String sql = """
        SELECT
            m.*,
            u.nombre || ' ' || u.apellido AS propietario
        FROM mascotas m
        INNER JOIN usuarios u
            ON m.id_dueno = u.id_usuario
        WHERE m.id_mascota = ?
        """;

        try (
                Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMascota);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    mascota = new Mascota();

                    mascota.setIdMascota(
                            rs.getInt("id_mascota"));

                    mascota.setNombre(
                            rs.getString("nombre"));

                    mascota.setEspecie(
                            rs.getString("especie"));

                    mascota.setRaza(
                            rs.getString("raza"));

                    if (rs.getDate("fecha_nacimiento") != null) {

                        mascota.setFechaNacimiento(
                                new java.util.Date(
                                        rs.getDate("fecha_nacimiento")
                                                .getTime()
                                )
                        );
                    }

                    mascota.setIdDueno(
                            rs.getInt("id_dueno"));

                    mascota.setNombreDueno(
                            rs.getString("propietario"));
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error buscar mascota por ID: "
                    + e.getMessage());
        }

        return mascota;
    }
}
