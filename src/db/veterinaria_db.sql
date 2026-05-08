--
-- PostgreSQL database dump
--

\restrict 8oqH15trpmfXfyfosI5sWdkapcOy8GKcZa4d3RbPGlMfzIDULqPeH5zc1tiqdlA

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

-- Started on 2026-05-08 17:23:41

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 41073)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 5192 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- TOC entry 921 (class 1247 OID 41118)
-- Name: tipo_estado_cita; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.tipo_estado_cita AS ENUM (
    'pendiente',
    'completada',
    'cancelada'
);


ALTER TYPE public.tipo_estado_cita OWNER TO postgres;

--
-- TOC entry 918 (class 1247 OID 41112)
-- Name: tipo_estado_usuario; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.tipo_estado_usuario AS ENUM (
    'activo',
    'inactivo'
);


ALTER TYPE public.tipo_estado_usuario OWNER TO postgres;

--
-- TOC entry 296 (class 1255 OID 41304)
-- Name: fn_listar_veterinarios_disponibles(timestamp without time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_listar_veterinarios_disponibles(p_fecha_hora timestamp without time zone) RETURNS TABLE(id_veterinario integer, nombre character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT v.id_veterinario, v.nombre
    FROM veterinarios v
    WHERE v.estado = 'activo'
      AND fn_veterinario_disponible(v.id_veterinario, p_fecha_hora);
END;
$$;


ALTER FUNCTION public.fn_listar_veterinarios_disponibles(p_fecha_hora timestamp without time zone) OWNER TO postgres;

--
-- TOC entry 291 (class 1255 OID 41297)
-- Name: fn_validar_cita_facturable(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_validar_cita_facturable(p_id_cita integer) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_estado tipo_estado_cita;
BEGIN
    SELECT estado INTO v_estado
    FROM citas
    WHERE id_cita = p_id_cita;

    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    RETURN v_estado = 'completada';
END;
$$;


ALTER FUNCTION public.fn_validar_cita_facturable(p_id_cita integer) OWNER TO postgres;

--
-- TOC entry 292 (class 1255 OID 41298)
-- Name: fn_validar_horario_atencion(timestamp without time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_validar_horario_atencion(p_fecha_hora timestamp without time zone) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_dia_semana INT;  
    v_hora       TIME;
BEGIN
    v_dia_semana := EXTRACT(DOW FROM p_fecha_hora)::INT;
    v_hora       := p_fecha_hora::TIME;

    -- Domingo (0) está fuera del horario
    IF v_dia_semana = 0 THEN
        RETURN FALSE;
    END IF;

    -- Lunes(1) a Sábado(6), entre 08:00 y 17:00
    IF v_hora >= '08:00:00' AND v_hora <= '17:00:00' THEN
        RETURN TRUE;
    END IF;

    RETURN FALSE;
END;
$$;


ALTER FUNCTION public.fn_validar_horario_atencion(p_fecha_hora timestamp without time zone) OWNER TO postgres;

--
-- TOC entry 289 (class 1255 OID 41295)
-- Name: fn_validar_limite_citas_usuario(integer, date); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_validar_limite_citas_usuario(p_id_usuario integer, p_fecha date) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_total INT;
BEGIN
    SELECT COUNT(*) INTO v_total
    FROM citas
    WHERE id_usuario     = p_id_usuario
      AND DATE(fecha_hora) = p_fecha
      AND estado IN ('pendiente', 'completada');

    RETURN v_total < 3;
END;
$$;


ALTER FUNCTION public.fn_validar_limite_citas_usuario(p_id_usuario integer, p_fecha date) OWNER TO postgres;

--
-- TOC entry 290 (class 1255 OID 41296)
-- Name: fn_veterinario_disponible(integer, timestamp without time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_veterinario_disponible(p_id_veterinario integer, p_fecha_hora timestamp without time zone) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_fin_bloque         TIMESTAMP;
    v_estado_vet         tipo_estado_usuario;
    v_conflicto_cita     INT := 0;
    v_conflicto_ausencia INT := 0;
BEGIN
    -- Cada cita bloquea 59 minutos (RN-01, RN-07)
    v_fin_bloque := p_fecha_hora + INTERVAL '59 minutes';

    -- Verificar que el veterinario esté activo (RN-10)
    SELECT estado INTO v_estado_vet
    FROM veterinarios
    WHERE id_veterinario = p_id_veterinario;

    IF NOT FOUND OR v_estado_vet <> 'activo' THEN
        RETURN FALSE;
    END IF;

    -- Verificar ausencias programadas (RN-10)
    SELECT COUNT(*) INTO v_conflicto_ausencia
    FROM ausencias_veterinario
    WHERE id_veterinario = p_id_veterinario
      AND p_fecha_hora < fecha_fin
      AND v_fin_bloque  > fecha_inicio;

    IF v_conflicto_ausencia > 0 THEN
        RETURN FALSE;
    END IF;

    -- Verificar solapamiento con citas existentes (RN-04)
    SELECT COUNT(*) INTO v_conflicto_cita
    FROM citas
    WHERE id_veterinario = p_id_veterinario
      AND estado = 'pendiente'
      AND p_fecha_hora < (fecha_hora + INTERVAL '59 minutes')
      AND v_fin_bloque  > fecha_hora;

    RETURN v_conflicto_cita = 0;
END;
$$;


ALTER FUNCTION public.fn_veterinario_disponible(p_id_veterinario integer, p_fecha_hora timestamp without time zone) OWNER TO postgres;

--
-- TOC entry 287 (class 1255 OID 41301)
-- Name: sp_actualizar_estado_cita(integer, public.tipo_estado_cita, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.sp_actualizar_estado_cita(p_id_cita integer, p_nuevo_estado public.tipo_estado_cita, p_id_usuario integer, OUT p_mensaje text) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_estado_actual tipo_estado_cita;
BEGIN
    SELECT estado INTO v_estado_actual
    FROM citas
    WHERE id_cita = p_id_cita;

    IF NOT FOUND THEN
        p_mensaje := 'ERROR: La cita no existe.';
        RETURN;
    END IF;

    -- Los estados completada y cancelada son finales
    IF v_estado_actual IN ('completada', 'cancelada') THEN
        p_mensaje := 'ERROR: No se puede cambiar el estado de una cita que ya está "' || v_estado_actual || '".';
        RETURN;
    END IF;

    IF v_estado_actual = p_nuevo_estado THEN
        p_mensaje := 'AVISO: La cita ya se encuentra en estado "' || v_estado_actual || '".';
        RETURN;
    END IF;

    UPDATE citas SET estado = p_nuevo_estado WHERE id_cita = p_id_cita;

    p_mensaje := 'Estado de cita #' || p_id_cita || ' actualizado a "' || p_nuevo_estado || '".';

EXCEPTION WHEN OTHERS THEN
    p_mensaje := 'Error interno: ' || SQLERRM;
END;
$$;


ALTER FUNCTION public.sp_actualizar_estado_cita(p_id_cita integer, p_nuevo_estado public.tipo_estado_cita, p_id_usuario integer, OUT p_mensaje text) OWNER TO postgres;

--
-- TOC entry 294 (class 1255 OID 41300)
-- Name: sp_cancelar_cita(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.sp_cancelar_cita(p_id_cita integer, p_id_usuario integer, OUT p_mensaje text) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_fecha_hora       TIMESTAMP;
    v_estado           tipo_estado_cita;
    v_id_usuario       INT;
    v_diferencia_horas NUMERIC;
BEGIN
    SELECT fecha_hora, estado, id_usuario
    INTO v_fecha_hora, v_estado, v_id_usuario
    FROM citas
    WHERE id_cita = p_id_cita;

    IF NOT FOUND THEN
        p_mensaje := 'ERROR: La cita no existe.';
        RETURN;
    END IF;

    -- Verificar que sea el dueño quien cancela (RN-09)
    IF v_id_usuario <> p_id_usuario THEN
        p_mensaje := 'ERROR RN-09: No tiene permiso para cancelar esta cita.';
        RETURN;
    END IF;

    -- Verificar que la cita esté pendiente
    IF v_estado <> 'pendiente' THEN
        p_mensaje := 'ERROR: No se puede cancelar una cita con estado "' || v_estado || '".';
        RETURN;
    END IF;

    -- Verificar anticipación mínima de 3 horas (RN-02)
    v_diferencia_horas := EXTRACT(EPOCH FROM (v_fecha_hora - NOW())) / 3600.0;

    IF v_diferencia_horas < 3 THEN
        p_mensaje := 'ERROR RN-02: La cita solo puede cancelarse con mínimo 3 horas de anticipación. ' ||
                     'Faltan ' || ROUND(v_diferencia_horas::NUMERIC, 2) || ' horas para la cita.';
        RETURN;
    END IF;

    UPDATE citas SET estado = 'cancelada' WHERE id_cita = p_id_cita;

    p_mensaje := 'Cita #' || p_id_cita || ' cancelada exitosamente.';

EXCEPTION WHEN OTHERS THEN
    p_mensaje := 'Error interno: ' || SQLERRM;
END;
$$;


ALTER FUNCTION public.sp_cancelar_cita(p_id_cita integer, p_id_usuario integer, OUT p_mensaje text) OWNER TO postgres;

--
-- TOC entry 293 (class 1255 OID 41299)
-- Name: sp_registrar_cita(integer, integer, integer, timestamp without time zone, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.sp_registrar_cita(p_id_usuario integer, p_id_mascota integer, p_id_especialidad integer, p_fecha_hora timestamp without time zone, p_motivo character varying, OUT p_id_cita integer, OUT p_mensaje text) RETURNS record
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_veterinario INT;
    v_id_dueno       INT;
    v_conflicto      INT;
BEGIN
    -- 1. Verificar horario de atención (RN-07)
    IF NOT fn_validar_horario_atencion(p_fecha_hora) THEN
        p_id_cita := NULL;
        p_mensaje := 'ERROR RN-07: La cita debe agendarse de lunes a sábado entre las 08:00 y las 17:00.';
        RETURN;
    END IF;

    -- 2. Verificar que la mascota exista y pertenezca al usuario (RN-03)
    SELECT id_dueno INTO v_id_dueno
    FROM mascotas
    WHERE id_mascota = p_id_mascota;

    IF NOT FOUND THEN
        p_id_cita := NULL;
        p_mensaje := 'ERROR: La mascota no existe en el sistema.';
        RETURN;
    END IF;

    IF v_id_dueno <> p_id_usuario THEN
        p_id_cita := NULL;
        p_mensaje := 'ERROR RN-03: La mascota no está vinculada al usuario indicado.';
        RETURN;
    END IF;

    -- 3. Verificar límite de citas del usuario (RN-08)
    IF NOT fn_validar_limite_citas_usuario(p_id_usuario, p_fecha_hora::DATE) THEN
        p_id_cita := NULL;
        p_mensaje := 'ERROR RN-08: El usuario ya tiene 3 citas agendadas para ese día.';
        RETURN;
    END IF;

    -- 4. Verificar que la mascota no tenga cita solapada (RN-01)
    SELECT COUNT(*) INTO v_conflicto
    FROM citas
    WHERE id_mascota = p_id_mascota
      AND estado = 'pendiente'
      AND p_fecha_hora < (fecha_hora + INTERVAL '59 minutes')
      AND (p_fecha_hora + INTERVAL '59 minutes') > fecha_hora;

    IF v_conflicto > 0 THEN
        p_id_cita := NULL;
        p_mensaje := 'ERROR RN-01: La mascota ya tiene una cita en ese intervalo de tiempo.';
        RETURN;
    END IF;

    -- 5. Asignación automática de veterinario disponible (RN-04, RN-06, RN-10)
    SELECT v.id_veterinario INTO v_id_veterinario
    FROM veterinarios v
    WHERE v.id_especialidad = p_id_especialidad
      AND fn_veterinario_disponible(v.id_veterinario, p_fecha_hora) = TRUE
    LIMIT 1;

    IF v_id_veterinario IS NULL THEN
        p_id_cita := NULL;
        p_mensaje := 'ERROR RN-04/RN-10: No hay veterinarios disponibles con esa especialidad en el horario solicitado.';
        RETURN;
    END IF;

    -- 6. Insertar la cita
    INSERT INTO citas (id_mascota, id_usuario, id_veterinario, id_especialidad, fecha_hora, estado, motivo_consulta)
    VALUES (p_id_mascota, p_id_usuario, v_id_veterinario, p_id_especialidad, p_fecha_hora, 'pendiente', p_motivo)
    RETURNING id_cita INTO p_id_cita;

    p_mensaje := 'Cita registrada exitosamente. ID: ' || p_id_cita ||
                 '. Veterinario asignado: #' || v_id_veterinario;

EXCEPTION WHEN OTHERS THEN
    p_id_cita := NULL;
    p_mensaje := 'Error interno: ' || SQLERRM;
END;
$$;


ALTER FUNCTION public.sp_registrar_cita(p_id_usuario integer, p_id_mascota integer, p_id_especialidad integer, p_fecha_hora timestamp without time zone, p_motivo character varying, OUT p_id_cita integer, OUT p_mensaje text) OWNER TO postgres;

--
-- TOC entry 295 (class 1255 OID 41302)
-- Name: sp_registrar_consulta(integer, integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.sp_registrar_consulta(p_id_cita integer, p_id_veterinario integer, p_diagnostico text, p_tratamiento text, p_observaciones text, OUT p_id_consulta integer, OUT p_mensaje text) RETURNS record
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_estado       tipo_estado_cita;
    v_vet_asignado INT;
BEGIN
    SELECT estado, id_veterinario
    INTO v_estado, v_vet_asignado
    FROM citas
    WHERE id_cita = p_id_cita;

    IF NOT FOUND THEN
        p_id_consulta := NULL;
        p_mensaje := 'ERROR: La cita no existe.';
        RETURN;
    END IF;

    -- Solo el veterinario asignado puede registrar la consulta (RN-09)
    IF v_vet_asignado <> p_id_veterinario THEN
        p_id_consulta := NULL;
        p_mensaje := 'ERROR RN-09: Solo el veterinario asignado puede registrar la consulta médica.';
        RETURN;
    END IF;

    -- La cita debe estar pendiente
    IF v_estado <> 'pendiente' THEN
        p_id_consulta := NULL;
        p_mensaje := 'ERROR: No se puede registrar consulta en una cita con estado "' || v_estado || '".';
        RETURN;
    END IF;

    -- Insertar consulta
    INSERT INTO consultas (id_cita, diagnostico, tratamiento, observaciones)
    VALUES (p_id_cita, p_diagnostico, p_tratamiento, p_observaciones)
    RETURNING id_consulta INTO p_id_consulta;

    -- Marcar la cita como completada para que el sistema externo pueda facturarla
    UPDATE citas SET estado = 'completada' WHERE id_cita = p_id_cita;

    p_mensaje := 'Consulta médica #' || p_id_consulta ||
                 ' registrada. Cita #' || p_id_cita ||
                 ' marcada como "completada" y disponible para facturación.';

EXCEPTION WHEN OTHERS THEN
    p_id_consulta := NULL;
    p_mensaje := 'Error interno: ' || SQLERRM;
END;
$$;


ALTER FUNCTION public.sp_registrar_consulta(p_id_cita integer, p_id_veterinario integer, p_diagnostico text, p_tratamiento text, p_observaciones text, OUT p_id_consulta integer, OUT p_mensaje text) OWNER TO postgres;

--
-- TOC entry 288 (class 1255 OID 41303)
-- Name: sp_validar_permisos_usuario(integer, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.sp_validar_permisos_usuario(p_id_usuario integer, p_accion character varying, OUT p_tiene_permiso boolean, OUT p_mensaje text) RETURNS record
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_rol VARCHAR(50);
BEGIN
    SELECT r.nombre_rol INTO v_rol
    FROM usuarios u
    JOIN roles r ON u.id_rol = r.id_rol
    WHERE u.id_usuario = p_id_usuario
      AND u.estado = 'activo';

    IF NOT FOUND THEN
        p_tiene_permiso := FALSE;
        p_mensaje := 'ERROR: Usuario no encontrado o inactivo.';
        RETURN;
    END IF;

    -- Matriz de permisos por rol
    p_tiene_permiso := CASE
    WHEN v_rol = 'Administrador' THEN TRUE

    WHEN v_rol = 'Veterinario' AND p_accion IN (
        'REGISTRAR_CONSULTA', 'CAMBIO_ESTADO_CITA', 'VER_HISTORIAL') THEN TRUE

    WHEN v_rol = 'Cliente' AND p_accion IN (
        'CANCELAR_CITA', 'VER_HISTORIAL_PROPIO', 'CREAR_CITA') THEN TRUE

    WHEN v_rol = 'Administrativo' AND p_accion IN (
        'CREAR_CITA',
        'CANCELAR_CITA',
        'CAMBIO_ESTADO_CITA',
        'VER_HISTORIAL'
    ) THEN TRUE

    ELSE FALSE
END;

    IF p_tiene_permiso THEN
        p_mensaje := 'Acceso concedido: ' || v_rol || ' puede ejecutar "' || p_accion || '".';
    ELSE
        p_mensaje := 'ACCESO DENEGADO: El rol "' || v_rol ||
                     '" no tiene permisos para ejecutar "' || p_accion || '".';
    END IF;
END;
$$;


ALTER FUNCTION public.sp_validar_permisos_usuario(p_id_usuario integer, p_accion character varying, OUT p_tiene_permiso boolean, OUT p_mensaje text) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 229 (class 1259 OID 41201)
-- Name: ausencias_veterinario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ausencias_veterinario (
    id_ausencia integer NOT NULL,
    id_veterinario integer NOT NULL,
    fecha_inicio timestamp without time zone NOT NULL,
    fecha_fin timestamp without time zone NOT NULL,
    motivo character varying(255),
    CONSTRAINT chk_ausencia_fechas CHECK ((fecha_fin > fecha_inicio))
);


ALTER TABLE public.ausencias_veterinario OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 41200)
-- Name: ausencias_veterinario_id_ausencia_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ausencias_veterinario_id_ausencia_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ausencias_veterinario_id_ausencia_seq OWNER TO postgres;

--
-- TOC entry 5241 (class 0 OID 0)
-- Dependencies: 228
-- Name: ausencias_veterinario_id_ausencia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ausencias_veterinario_id_ausencia_seq OWNED BY public.ausencias_veterinario.id_ausencia;


--
-- TOC entry 233 (class 1259 OID 41236)
-- Name: citas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.citas (
    id_cita integer NOT NULL,
    id_mascota integer NOT NULL,
    id_usuario integer NOT NULL,
    id_veterinario integer NOT NULL,
    id_especialidad integer NOT NULL,
    fecha_hora timestamp without time zone NOT NULL,
    estado public.tipo_estado_cita DEFAULT 'pendiente'::public.tipo_estado_cita NOT NULL,
    motivo_consulta character varying(500),
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.citas OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 41235)
-- Name: citas_id_cita_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.citas_id_cita_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.citas_id_cita_seq OWNER TO postgres;

--
-- TOC entry 5244 (class 0 OID 0)
-- Dependencies: 232
-- Name: citas_id_cita_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.citas_id_cita_seq OWNED BY public.citas.id_cita;


--
-- TOC entry 235 (class 1259 OID 41275)
-- Name: consultas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.consultas (
    id_consulta integer NOT NULL,
    id_cita integer NOT NULL,
    diagnostico text NOT NULL,
    tratamiento text,
    observaciones text,
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.consultas OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 41274)
-- Name: consultas_id_consulta_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.consultas_id_consulta_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.consultas_id_consulta_seq OWNER TO postgres;

--
-- TOC entry 5247 (class 0 OID 0)
-- Dependencies: 234
-- Name: consultas_id_consulta_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.consultas_id_consulta_seq OWNED BY public.consultas.id_consulta;


--
-- TOC entry 225 (class 1259 OID 41163)
-- Name: especialidades; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.especialidades (
    id_especialidad integer NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion character varying(255)
);


ALTER TABLE public.especialidades OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 41162)
-- Name: especialidades_id_especialidad_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.especialidades_id_especialidad_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.especialidades_id_especialidad_seq OWNER TO postgres;

--
-- TOC entry 5250 (class 0 OID 0)
-- Dependencies: 224
-- Name: especialidades_id_especialidad_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.especialidades_id_especialidad_seq OWNED BY public.especialidades.id_especialidad;


--
-- TOC entry 231 (class 1259 OID 41218)
-- Name: mascotas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mascotas (
    id_mascota integer NOT NULL,
    nombre character varying(100) NOT NULL,
    especie character varying(50) NOT NULL,
    raza character varying(100),
    fecha_nacimiento date,
    id_dueno integer NOT NULL,
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.mascotas OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 41217)
-- Name: mascotas_id_mascota_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.mascotas_id_mascota_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mascotas_id_mascota_seq OWNER TO postgres;

--
-- TOC entry 5253 (class 0 OID 0)
-- Dependencies: 230
-- Name: mascotas_id_mascota_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.mascotas_id_mascota_seq OWNED BY public.mascotas.id_mascota;


--
-- TOC entry 237 (class 1259 OID 57688)
-- Name: permisos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permisos (
    id_permiso integer NOT NULL,
    nombre_permiso character varying(100) NOT NULL
);


ALTER TABLE public.permisos OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 57687)
-- Name: permisos_id_permiso_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.permisos_id_permiso_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.permisos_id_permiso_seq OWNER TO postgres;

--
-- TOC entry 5256 (class 0 OID 0)
-- Dependencies: 236
-- Name: permisos_id_permiso_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.permisos_id_permiso_seq OWNED BY public.permisos.id_permiso;


--
-- TOC entry 221 (class 1259 OID 41126)
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id_rol integer NOT NULL,
    nombre_rol character varying(50) NOT NULL,
    descripcion character varying(255)
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 41125)
-- Name: roles_id_rol_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_id_rol_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_id_rol_seq OWNER TO postgres;

--
-- TOC entry 5259 (class 0 OID 0)
-- Dependencies: 220
-- Name: roles_id_rol_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_id_rol_seq OWNED BY public.roles.id_rol;


--
-- TOC entry 238 (class 1259 OID 57698)
-- Name: roles_permisos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles_permisos (
    id_rol integer NOT NULL,
    id_permiso integer NOT NULL
);


ALTER TABLE public.roles_permisos OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 41137)
-- Name: usuarios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuarios (
    id_usuario integer NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    telefono character varying(20),
    contrasena character varying(255) NOT NULL,
    id_rol integer NOT NULL,
    estado public.tipo_estado_usuario DEFAULT 'activo'::public.tipo_estado_usuario NOT NULL,
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.usuarios OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 41136)
-- Name: usuarios_id_usuario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuarios_id_usuario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuarios_id_usuario_seq OWNER TO postgres;

--
-- TOC entry 5263 (class 0 OID 0)
-- Dependencies: 222
-- Name: usuarios_id_usuario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuarios_id_usuario_seq OWNED BY public.usuarios.id_usuario;


--
-- TOC entry 227 (class 1259 OID 41174)
-- Name: veterinarios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.veterinarios (
    id_veterinario integer NOT NULL,
    id_usuario integer NOT NULL,
    id_especialidad integer NOT NULL,
    numero_registro character varying(50) NOT NULL,
    estado public.tipo_estado_usuario DEFAULT 'activo'::public.tipo_estado_usuario NOT NULL
);


ALTER TABLE public.veterinarios OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 41173)
-- Name: veterinarios_id_veterinario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.veterinarios_id_veterinario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.veterinarios_id_veterinario_seq OWNER TO postgres;

--
-- TOC entry 5266 (class 0 OID 0)
-- Dependencies: 226
-- Name: veterinarios_id_veterinario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.veterinarios_id_veterinario_seq OWNED BY public.veterinarios.id_veterinario;


--
-- TOC entry 4964 (class 2604 OID 41204)
-- Name: ausencias_veterinario id_ausencia; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ausencias_veterinario ALTER COLUMN id_ausencia SET DEFAULT nextval('public.ausencias_veterinario_id_ausencia_seq'::regclass);


--
-- TOC entry 4967 (class 2604 OID 41239)
-- Name: citas id_cita; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.citas ALTER COLUMN id_cita SET DEFAULT nextval('public.citas_id_cita_seq'::regclass);


--
-- TOC entry 4970 (class 2604 OID 41278)
-- Name: consultas id_consulta; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consultas ALTER COLUMN id_consulta SET DEFAULT nextval('public.consultas_id_consulta_seq'::regclass);


--
-- TOC entry 4961 (class 2604 OID 41166)
-- Name: especialidades id_especialidad; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.especialidades ALTER COLUMN id_especialidad SET DEFAULT nextval('public.especialidades_id_especialidad_seq'::regclass);


--
-- TOC entry 4965 (class 2604 OID 41221)
-- Name: mascotas id_mascota; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mascotas ALTER COLUMN id_mascota SET DEFAULT nextval('public.mascotas_id_mascota_seq'::regclass);


--
-- TOC entry 4972 (class 2604 OID 57691)
-- Name: permisos id_permiso; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permisos ALTER COLUMN id_permiso SET DEFAULT nextval('public.permisos_id_permiso_seq'::regclass);


--
-- TOC entry 4957 (class 2604 OID 41129)
-- Name: roles id_rol; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN id_rol SET DEFAULT nextval('public.roles_id_rol_seq'::regclass);


--
-- TOC entry 4958 (class 2604 OID 41140)
-- Name: usuarios id_usuario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id_usuario SET DEFAULT nextval('public.usuarios_id_usuario_seq'::regclass);


--
-- TOC entry 4962 (class 2604 OID 41177)
-- Name: veterinarios id_veterinario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.veterinarios ALTER COLUMN id_veterinario SET DEFAULT nextval('public.veterinarios_id_veterinario_seq'::regclass);


--
-- TOC entry 5176 (class 0 OID 41201)
-- Dependencies: 229
-- Data for Name: ausencias_veterinario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ausencias_veterinario (id_ausencia, id_veterinario, fecha_inicio, fecha_fin, motivo) FROM stdin;
\.


--
-- TOC entry 5180 (class 0 OID 41236)
-- Dependencies: 233
-- Data for Name: citas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.citas (id_cita, id_mascota, id_usuario, id_veterinario, id_especialidad, fecha_hora, estado, motivo_consulta, fecha_creacion) FROM stdin;
\.


--
-- TOC entry 5182 (class 0 OID 41275)
-- Dependencies: 235
-- Data for Name: consultas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.consultas (id_consulta, id_cita, diagnostico, tratamiento, observaciones, fecha_registro) FROM stdin;
\.


--
-- TOC entry 5172 (class 0 OID 41163)
-- Dependencies: 225
-- Data for Name: especialidades; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.especialidades (id_especialidad, nombre, descripcion) FROM stdin;
1	General	Consulta general y preventiva
2	Cirugia	Procedimientos quirúrgicos
3	Vacunacion	Aplicación de vacunas y refuerzos
4	Dermatologia	Enfermedades de piel y pelo
5	Odontologia	Salud dental veterinaria
\.


--
-- TOC entry 5178 (class 0 OID 41218)
-- Dependencies: 231
-- Data for Name: mascotas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mascotas (id_mascota, nombre, especie, raza, fecha_nacimiento, id_dueno, fecha_registro) FROM stdin;
\.


--
-- TOC entry 5184 (class 0 OID 57688)
-- Dependencies: 237
-- Data for Name: permisos; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.permisos (id_permiso, nombre_permiso) FROM stdin;
1	GESTION_USUARIOS
2	GESTION_MASCOTAS
3	GESTION_CITAS
\.


--
-- TOC entry 5168 (class 0 OID 41126)
-- Dependencies: 221
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id_rol, nombre_rol, descripcion) FROM stdin;
1	Administrador	Acceso completo al sistema
2	Veterinario	Gestiona citas y consultas médicas
3	Administrativo	Gestiona citas
4	Cliente	Agenda citas y consulta historial de sus mascotas
\.


--
-- TOC entry 5185 (class 0 OID 57698)
-- Dependencies: 238
-- Data for Name: roles_permisos; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles_permisos (id_rol, id_permiso) FROM stdin;
\.


--
-- TOC entry 5170 (class 0 OID 41137)
-- Dependencies: 223
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuarios (id_usuario, nombre, apellido, email, telefono, contrasena, id_rol, estado, fecha_registro) FROM stdin;
1	Juan	Pérez	juan@gmail.com	3001234567	$2a$06$MdjWTMIiq7sT9I5pNXIPk./KaaL/ZgnSxe23grAjJ/Up1KCxpPuBy	4	activo	2026-04-23 18:32:37.353101
2	Ana	Gómez	ana@gmail.com	3007654321	$2a$06$WMlrCXdi4awuL9OP1Yo19uLowdTgB6urX.YyLZ.JMVMNGuFsd3uqq	4	activo	2026-04-23 18:32:37.353101
3	Carlos	Figueroa	carlos@gmail.com	3000000000	$2a$06$IpOn0S65ogV7uQtxDopbEu5wXj0gP8TcDlTbJsb/pJEGQQPiOqRGW	1	activo	2026-04-23 18:32:37.353101
4	Laura	Marquez	laumarquez@gmail.com	3001111111	$2a$06$81ejObrONAJnfTQVuFAOOuOFWu8m5i.axH9TfJDESRbyyjVF20bc2	2	activo	2026-04-23 18:32:37.353101
5	Sofía	Ortega	sofiortega@gmail.com	3002222222	$2a$06$d5YUz4KJtsin0lcxZzIllOHFZJPwEnWqqt25CT4D2KDhCQbl3pmz6	3	activo	2026-04-23 18:32:37.353101
6	Jhonatan	Motta	jhonmotta@gmail.com	3155491297	1234	1	activo	2026-05-08 16:04:20.010499
7	Sam	Pe	sampe@gmail.com	3164987643	$2a$06$AB.5B9TKihzKAhf/q0LExe65NUMjiLDxiWBgRu7lWLw5FOSqiLmLW	1	activo	2026-05-08 16:10:32.506276
8	Erika	Duarte	erdua@gmail.com	3164569832	$2a$06$ozcMOf/DB8CE7ZV/752OneBnQqnJZtSnzKOzfrxIpnSHLRXVmxI0e	2	activo	2026-05-08 16:11:59.377978
\.


--
-- TOC entry 5174 (class 0 OID 41174)
-- Dependencies: 227
-- Data for Name: veterinarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.veterinarios (id_veterinario, id_usuario, id_especialidad, numero_registro, estado) FROM stdin;
\.


--
-- TOC entry 5268 (class 0 OID 0)
-- Dependencies: 228
-- Name: ausencias_veterinario_id_ausencia_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ausencias_veterinario_id_ausencia_seq', 1, false);


--
-- TOC entry 5269 (class 0 OID 0)
-- Dependencies: 232
-- Name: citas_id_cita_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.citas_id_cita_seq', 1, false);


--
-- TOC entry 5270 (class 0 OID 0)
-- Dependencies: 234
-- Name: consultas_id_consulta_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.consultas_id_consulta_seq', 1, false);


--
-- TOC entry 5271 (class 0 OID 0)
-- Dependencies: 224
-- Name: especialidades_id_especialidad_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.especialidades_id_especialidad_seq', 5, true);


--
-- TOC entry 5272 (class 0 OID 0)
-- Dependencies: 230
-- Name: mascotas_id_mascota_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mascotas_id_mascota_seq', 1, false);


--
-- TOC entry 5273 (class 0 OID 0)
-- Dependencies: 236
-- Name: permisos_id_permiso_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.permisos_id_permiso_seq', 3, true);


--
-- TOC entry 5274 (class 0 OID 0)
-- Dependencies: 220
-- Name: roles_id_rol_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_id_rol_seq', 4, true);


--
-- TOC entry 5275 (class 0 OID 0)
-- Dependencies: 222
-- Name: usuarios_id_usuario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuarios_id_usuario_seq', 8, true);


--
-- TOC entry 5276 (class 0 OID 0)
-- Dependencies: 226
-- Name: veterinarios_id_veterinario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.veterinarios_id_veterinario_seq', 1, false);


--
-- TOC entry 4993 (class 2606 OID 41211)
-- Name: ausencias_veterinario ausencias_veterinario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ausencias_veterinario
    ADD CONSTRAINT ausencias_veterinario_pkey PRIMARY KEY (id_ausencia);


--
-- TOC entry 4997 (class 2606 OID 41253)
-- Name: citas citas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.citas
    ADD CONSTRAINT citas_pkey PRIMARY KEY (id_cita);


--
-- TOC entry 4999 (class 2606 OID 41289)
-- Name: consultas consultas_id_cita_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consultas
    ADD CONSTRAINT consultas_id_cita_key UNIQUE (id_cita);


--
-- TOC entry 5001 (class 2606 OID 41287)
-- Name: consultas consultas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consultas
    ADD CONSTRAINT consultas_pkey PRIMARY KEY (id_consulta);


--
-- TOC entry 4983 (class 2606 OID 41172)
-- Name: especialidades especialidades_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.especialidades
    ADD CONSTRAINT especialidades_nombre_key UNIQUE (nombre);


--
-- TOC entry 4985 (class 2606 OID 41170)
-- Name: especialidades especialidades_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.especialidades
    ADD CONSTRAINT especialidades_pkey PRIMARY KEY (id_especialidad);


--
-- TOC entry 4995 (class 2606 OID 41229)
-- Name: mascotas mascotas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mascotas
    ADD CONSTRAINT mascotas_pkey PRIMARY KEY (id_mascota);


--
-- TOC entry 5003 (class 2606 OID 57697)
-- Name: permisos permisos_nombre_permiso_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permisos
    ADD CONSTRAINT permisos_nombre_permiso_key UNIQUE (nombre_permiso);


--
-- TOC entry 5005 (class 2606 OID 57695)
-- Name: permisos permisos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permisos
    ADD CONSTRAINT permisos_pkey PRIMARY KEY (id_permiso);


--
-- TOC entry 4975 (class 2606 OID 41135)
-- Name: roles roles_nombre_rol_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_nombre_rol_key UNIQUE (nombre_rol);


--
-- TOC entry 5007 (class 2606 OID 57704)
-- Name: roles_permisos roles_permisos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles_permisos
    ADD CONSTRAINT roles_permisos_pkey PRIMARY KEY (id_rol, id_permiso);


--
-- TOC entry 4977 (class 2606 OID 41133)
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id_rol);


--
-- TOC entry 4979 (class 2606 OID 41156)
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- TOC entry 4981 (class 2606 OID 41154)
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id_usuario);


--
-- TOC entry 4987 (class 2606 OID 41187)
-- Name: veterinarios veterinarios_id_usuario_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.veterinarios
    ADD CONSTRAINT veterinarios_id_usuario_key UNIQUE (id_usuario);


--
-- TOC entry 4989 (class 2606 OID 41189)
-- Name: veterinarios veterinarios_numero_registro_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.veterinarios
    ADD CONSTRAINT veterinarios_numero_registro_key UNIQUE (numero_registro);


--
-- TOC entry 4991 (class 2606 OID 41185)
-- Name: veterinarios veterinarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.veterinarios
    ADD CONSTRAINT veterinarios_pkey PRIMARY KEY (id_veterinario);


--
-- TOC entry 5011 (class 2606 OID 41212)
-- Name: ausencias_veterinario fk_ausencia_vet; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ausencias_veterinario
    ADD CONSTRAINT fk_ausencia_vet FOREIGN KEY (id_veterinario) REFERENCES public.veterinarios(id_veterinario);


--
-- TOC entry 5013 (class 2606 OID 41269)
-- Name: citas fk_cita_especialidad; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.citas
    ADD CONSTRAINT fk_cita_especialidad FOREIGN KEY (id_especialidad) REFERENCES public.especialidades(id_especialidad);


--
-- TOC entry 5014 (class 2606 OID 41254)
-- Name: citas fk_cita_mascota; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.citas
    ADD CONSTRAINT fk_cita_mascota FOREIGN KEY (id_mascota) REFERENCES public.mascotas(id_mascota);


--
-- TOC entry 5015 (class 2606 OID 41259)
-- Name: citas fk_cita_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.citas
    ADD CONSTRAINT fk_cita_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario);


--
-- TOC entry 5016 (class 2606 OID 41264)
-- Name: citas fk_cita_veterinario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.citas
    ADD CONSTRAINT fk_cita_veterinario FOREIGN KEY (id_veterinario) REFERENCES public.veterinarios(id_veterinario);


--
-- TOC entry 5017 (class 2606 OID 41290)
-- Name: consultas fk_consulta_cita; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consultas
    ADD CONSTRAINT fk_consulta_cita FOREIGN KEY (id_cita) REFERENCES public.citas(id_cita);


--
-- TOC entry 5012 (class 2606 OID 41230)
-- Name: mascotas fk_mascota_dueno; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mascotas
    ADD CONSTRAINT fk_mascota_dueno FOREIGN KEY (id_dueno) REFERENCES public.usuarios(id_usuario);


--
-- TOC entry 5008 (class 2606 OID 41157)
-- Name: usuarios fk_usuario_rol; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES public.roles(id_rol);


--
-- TOC entry 5009 (class 2606 OID 41195)
-- Name: veterinarios fk_vet_especialidad; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.veterinarios
    ADD CONSTRAINT fk_vet_especialidad FOREIGN KEY (id_especialidad) REFERENCES public.especialidades(id_especialidad);


--
-- TOC entry 5010 (class 2606 OID 41190)
-- Name: veterinarios fk_vet_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.veterinarios
    ADD CONSTRAINT fk_vet_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario);


--
-- TOC entry 5018 (class 2606 OID 57710)
-- Name: roles_permisos roles_permisos_id_permiso_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles_permisos
    ADD CONSTRAINT roles_permisos_id_permiso_fkey FOREIGN KEY (id_permiso) REFERENCES public.permisos(id_permiso);


--
-- TOC entry 5019 (class 2606 OID 57705)
-- Name: roles_permisos roles_permisos_id_rol_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles_permisos
    ADD CONSTRAINT roles_permisos_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.roles(id_rol);


--
-- TOC entry 5191 (class 0 OID 0)
-- Dependencies: 6
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

GRANT ALL ON SCHEMA public TO admin_veterinaria;


--
-- TOC entry 5193 (class 0 OID 0)
-- Dependencies: 277
-- Name: FUNCTION armor(bytea); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.armor(bytea) TO admin_veterinaria;


--
-- TOC entry 5194 (class 0 OID 0)
-- Dependencies: 284
-- Name: FUNCTION armor(bytea, text[], text[]); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.armor(bytea, text[], text[]) TO admin_veterinaria;


--
-- TOC entry 5195 (class 0 OID 0)
-- Dependencies: 245
-- Name: FUNCTION crypt(text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.crypt(text, text) TO admin_veterinaria;


--
-- TOC entry 5196 (class 0 OID 0)
-- Dependencies: 285
-- Name: FUNCTION dearmor(text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.dearmor(text) TO admin_veterinaria;


--
-- TOC entry 5197 (class 0 OID 0)
-- Dependencies: 249
-- Name: FUNCTION decrypt(bytea, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.decrypt(bytea, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5198 (class 0 OID 0)
-- Dependencies: 251
-- Name: FUNCTION decrypt_iv(bytea, bytea, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.decrypt_iv(bytea, bytea, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5199 (class 0 OID 0)
-- Dependencies: 242
-- Name: FUNCTION digest(bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.digest(bytea, text) TO admin_veterinaria;


--
-- TOC entry 5200 (class 0 OID 0)
-- Dependencies: 241
-- Name: FUNCTION digest(text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.digest(text, text) TO admin_veterinaria;


--
-- TOC entry 5201 (class 0 OID 0)
-- Dependencies: 248
-- Name: FUNCTION encrypt(bytea, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.encrypt(bytea, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5202 (class 0 OID 0)
-- Dependencies: 250
-- Name: FUNCTION encrypt_iv(bytea, bytea, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.encrypt_iv(bytea, bytea, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5203 (class 0 OID 0)
-- Dependencies: 240
-- Name: FUNCTION fips_mode(); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.fips_mode() TO admin_veterinaria;


--
-- TOC entry 5204 (class 0 OID 0)
-- Dependencies: 296
-- Name: FUNCTION fn_listar_veterinarios_disponibles(p_fecha_hora timestamp without time zone); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.fn_listar_veterinarios_disponibles(p_fecha_hora timestamp without time zone) TO admin_veterinaria;


--
-- TOC entry 5205 (class 0 OID 0)
-- Dependencies: 291
-- Name: FUNCTION fn_validar_cita_facturable(p_id_cita integer); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.fn_validar_cita_facturable(p_id_cita integer) TO admin_veterinaria;


--
-- TOC entry 5206 (class 0 OID 0)
-- Dependencies: 292
-- Name: FUNCTION fn_validar_horario_atencion(p_fecha_hora timestamp without time zone); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.fn_validar_horario_atencion(p_fecha_hora timestamp without time zone) TO admin_veterinaria;


--
-- TOC entry 5207 (class 0 OID 0)
-- Dependencies: 289
-- Name: FUNCTION fn_validar_limite_citas_usuario(p_id_usuario integer, p_fecha date); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.fn_validar_limite_citas_usuario(p_id_usuario integer, p_fecha date) TO admin_veterinaria;


--
-- TOC entry 5208 (class 0 OID 0)
-- Dependencies: 290
-- Name: FUNCTION fn_veterinario_disponible(p_id_veterinario integer, p_fecha_hora timestamp without time zone); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.fn_veterinario_disponible(p_id_veterinario integer, p_fecha_hora timestamp without time zone) TO admin_veterinaria;


--
-- TOC entry 5209 (class 0 OID 0)
-- Dependencies: 252
-- Name: FUNCTION gen_random_bytes(integer); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.gen_random_bytes(integer) TO admin_veterinaria;


--
-- TOC entry 5210 (class 0 OID 0)
-- Dependencies: 253
-- Name: FUNCTION gen_random_uuid(); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.gen_random_uuid() TO admin_veterinaria;


--
-- TOC entry 5211 (class 0 OID 0)
-- Dependencies: 246
-- Name: FUNCTION gen_salt(text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.gen_salt(text) TO admin_veterinaria;


--
-- TOC entry 5212 (class 0 OID 0)
-- Dependencies: 247
-- Name: FUNCTION gen_salt(text, integer); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.gen_salt(text, integer) TO admin_veterinaria;


--
-- TOC entry 5213 (class 0 OID 0)
-- Dependencies: 244
-- Name: FUNCTION hmac(bytea, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.hmac(bytea, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5214 (class 0 OID 0)
-- Dependencies: 243
-- Name: FUNCTION hmac(text, text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.hmac(text, text, text) TO admin_veterinaria;


--
-- TOC entry 5215 (class 0 OID 0)
-- Dependencies: 239
-- Name: FUNCTION pgp_armor_headers(text, OUT key text, OUT value text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_armor_headers(text, OUT key text, OUT value text) TO admin_veterinaria;


--
-- TOC entry 5216 (class 0 OID 0)
-- Dependencies: 276
-- Name: FUNCTION pgp_key_id(bytea); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_key_id(bytea) TO admin_veterinaria;


--
-- TOC entry 5217 (class 0 OID 0)
-- Dependencies: 266
-- Name: FUNCTION pgp_pub_decrypt(bytea, bytea); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_decrypt(bytea, bytea) TO admin_veterinaria;


--
-- TOC entry 5218 (class 0 OID 0)
-- Dependencies: 268
-- Name: FUNCTION pgp_pub_decrypt(bytea, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_decrypt(bytea, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5219 (class 0 OID 0)
-- Dependencies: 270
-- Name: FUNCTION pgp_pub_decrypt(bytea, bytea, text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text) TO admin_veterinaria;


--
-- TOC entry 5220 (class 0 OID 0)
-- Dependencies: 267
-- Name: FUNCTION pgp_pub_decrypt_bytea(bytea, bytea); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea) TO admin_veterinaria;


--
-- TOC entry 5221 (class 0 OID 0)
-- Dependencies: 269
-- Name: FUNCTION pgp_pub_decrypt_bytea(bytea, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5222 (class 0 OID 0)
-- Dependencies: 275
-- Name: FUNCTION pgp_pub_decrypt_bytea(bytea, bytea, text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text) TO admin_veterinaria;


--
-- TOC entry 5223 (class 0 OID 0)
-- Dependencies: 262
-- Name: FUNCTION pgp_pub_encrypt(text, bytea); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_encrypt(text, bytea) TO admin_veterinaria;


--
-- TOC entry 5224 (class 0 OID 0)
-- Dependencies: 264
-- Name: FUNCTION pgp_pub_encrypt(text, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_encrypt(text, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5225 (class 0 OID 0)
-- Dependencies: 263
-- Name: FUNCTION pgp_pub_encrypt_bytea(bytea, bytea); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea) TO admin_veterinaria;


--
-- TOC entry 5226 (class 0 OID 0)
-- Dependencies: 265
-- Name: FUNCTION pgp_pub_encrypt_bytea(bytea, bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text) TO admin_veterinaria;


--
-- TOC entry 5227 (class 0 OID 0)
-- Dependencies: 258
-- Name: FUNCTION pgp_sym_decrypt(bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_sym_decrypt(bytea, text) TO admin_veterinaria;


--
-- TOC entry 5228 (class 0 OID 0)
-- Dependencies: 260
-- Name: FUNCTION pgp_sym_decrypt(bytea, text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_sym_decrypt(bytea, text, text) TO admin_veterinaria;


--
-- TOC entry 5229 (class 0 OID 0)
-- Dependencies: 259
-- Name: FUNCTION pgp_sym_decrypt_bytea(bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_sym_decrypt_bytea(bytea, text) TO admin_veterinaria;


--
-- TOC entry 5230 (class 0 OID 0)
-- Dependencies: 261
-- Name: FUNCTION pgp_sym_decrypt_bytea(bytea, text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text) TO admin_veterinaria;


--
-- TOC entry 5231 (class 0 OID 0)
-- Dependencies: 254
-- Name: FUNCTION pgp_sym_encrypt(text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_sym_encrypt(text, text) TO admin_veterinaria;


--
-- TOC entry 5232 (class 0 OID 0)
-- Dependencies: 256
-- Name: FUNCTION pgp_sym_encrypt(text, text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_sym_encrypt(text, text, text) TO admin_veterinaria;


--
-- TOC entry 5233 (class 0 OID 0)
-- Dependencies: 255
-- Name: FUNCTION pgp_sym_encrypt_bytea(bytea, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_sym_encrypt_bytea(bytea, text) TO admin_veterinaria;


--
-- TOC entry 5234 (class 0 OID 0)
-- Dependencies: 257
-- Name: FUNCTION pgp_sym_encrypt_bytea(bytea, text, text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text) TO admin_veterinaria;


--
-- TOC entry 5235 (class 0 OID 0)
-- Dependencies: 287
-- Name: FUNCTION sp_actualizar_estado_cita(p_id_cita integer, p_nuevo_estado public.tipo_estado_cita, p_id_usuario integer, OUT p_mensaje text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.sp_actualizar_estado_cita(p_id_cita integer, p_nuevo_estado public.tipo_estado_cita, p_id_usuario integer, OUT p_mensaje text) TO admin_veterinaria;


--
-- TOC entry 5236 (class 0 OID 0)
-- Dependencies: 294
-- Name: FUNCTION sp_cancelar_cita(p_id_cita integer, p_id_usuario integer, OUT p_mensaje text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.sp_cancelar_cita(p_id_cita integer, p_id_usuario integer, OUT p_mensaje text) TO admin_veterinaria;


--
-- TOC entry 5237 (class 0 OID 0)
-- Dependencies: 293
-- Name: FUNCTION sp_registrar_cita(p_id_usuario integer, p_id_mascota integer, p_id_especialidad integer, p_fecha_hora timestamp without time zone, p_motivo character varying, OUT p_id_cita integer, OUT p_mensaje text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.sp_registrar_cita(p_id_usuario integer, p_id_mascota integer, p_id_especialidad integer, p_fecha_hora timestamp without time zone, p_motivo character varying, OUT p_id_cita integer, OUT p_mensaje text) TO admin_veterinaria;


--
-- TOC entry 5238 (class 0 OID 0)
-- Dependencies: 295
-- Name: FUNCTION sp_registrar_consulta(p_id_cita integer, p_id_veterinario integer, p_diagnostico text, p_tratamiento text, p_observaciones text, OUT p_id_consulta integer, OUT p_mensaje text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.sp_registrar_consulta(p_id_cita integer, p_id_veterinario integer, p_diagnostico text, p_tratamiento text, p_observaciones text, OUT p_id_consulta integer, OUT p_mensaje text) TO admin_veterinaria;


--
-- TOC entry 5239 (class 0 OID 0)
-- Dependencies: 288
-- Name: FUNCTION sp_validar_permisos_usuario(p_id_usuario integer, p_accion character varying, OUT p_tiene_permiso boolean, OUT p_mensaje text); Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON FUNCTION public.sp_validar_permisos_usuario(p_id_usuario integer, p_accion character varying, OUT p_tiene_permiso boolean, OUT p_mensaje text) TO admin_veterinaria;


--
-- TOC entry 5240 (class 0 OID 0)
-- Dependencies: 229
-- Name: TABLE ausencias_veterinario; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.ausencias_veterinario TO admin_veterinaria;


--
-- TOC entry 5242 (class 0 OID 0)
-- Dependencies: 228
-- Name: SEQUENCE ausencias_veterinario_id_ausencia_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ausencias_veterinario_id_ausencia_seq TO admin_veterinaria;


--
-- TOC entry 5243 (class 0 OID 0)
-- Dependencies: 233
-- Name: TABLE citas; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.citas TO admin_veterinaria;


--
-- TOC entry 5245 (class 0 OID 0)
-- Dependencies: 232
-- Name: SEQUENCE citas_id_cita_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.citas_id_cita_seq TO admin_veterinaria;


--
-- TOC entry 5246 (class 0 OID 0)
-- Dependencies: 235
-- Name: TABLE consultas; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.consultas TO admin_veterinaria;


--
-- TOC entry 5248 (class 0 OID 0)
-- Dependencies: 234
-- Name: SEQUENCE consultas_id_consulta_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.consultas_id_consulta_seq TO admin_veterinaria;


--
-- TOC entry 5249 (class 0 OID 0)
-- Dependencies: 225
-- Name: TABLE especialidades; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.especialidades TO admin_veterinaria;


--
-- TOC entry 5251 (class 0 OID 0)
-- Dependencies: 224
-- Name: SEQUENCE especialidades_id_especialidad_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.especialidades_id_especialidad_seq TO admin_veterinaria;


--
-- TOC entry 5252 (class 0 OID 0)
-- Dependencies: 231
-- Name: TABLE mascotas; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.mascotas TO admin_veterinaria;


--
-- TOC entry 5254 (class 0 OID 0)
-- Dependencies: 230
-- Name: SEQUENCE mascotas_id_mascota_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.mascotas_id_mascota_seq TO admin_veterinaria;


--
-- TOC entry 5255 (class 0 OID 0)
-- Dependencies: 237
-- Name: TABLE permisos; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.permisos TO admin_veterinaria;


--
-- TOC entry 5257 (class 0 OID 0)
-- Dependencies: 236
-- Name: SEQUENCE permisos_id_permiso_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.permisos_id_permiso_seq TO admin_veterinaria;


--
-- TOC entry 5258 (class 0 OID 0)
-- Dependencies: 221
-- Name: TABLE roles; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.roles TO admin_veterinaria;


--
-- TOC entry 5260 (class 0 OID 0)
-- Dependencies: 220
-- Name: SEQUENCE roles_id_rol_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.roles_id_rol_seq TO admin_veterinaria;


--
-- TOC entry 5261 (class 0 OID 0)
-- Dependencies: 238
-- Name: TABLE roles_permisos; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.roles_permisos TO admin_veterinaria;


--
-- TOC entry 5262 (class 0 OID 0)
-- Dependencies: 223
-- Name: TABLE usuarios; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.usuarios TO admin_veterinaria;


--
-- TOC entry 5264 (class 0 OID 0)
-- Dependencies: 222
-- Name: SEQUENCE usuarios_id_usuario_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.usuarios_id_usuario_seq TO admin_veterinaria;


--
-- TOC entry 5265 (class 0 OID 0)
-- Dependencies: 227
-- Name: TABLE veterinarios; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.veterinarios TO admin_veterinaria;


--
-- TOC entry 5267 (class 0 OID 0)
-- Dependencies: 226
-- Name: SEQUENCE veterinarios_id_veterinario_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.veterinarios_id_veterinario_seq TO admin_veterinaria;


--
-- TOC entry 2151 (class 826 OID 57685)
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO admin_veterinaria;


--
-- TOC entry 2152 (class 826 OID 57686)
-- Name: DEFAULT PRIVILEGES FOR FUNCTIONS; Type: DEFAULT ACL; Schema: public; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON FUNCTIONS TO admin_veterinaria;


--
-- TOC entry 2150 (class 826 OID 57684)
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO admin_veterinaria;


-- Completed on 2026-05-08 17:23:41

--
-- PostgreSQL database dump complete
--

\unrestrict 8oqH15trpmfXfyfosI5sWdkapcOy8GKcZa4d3RbPGlMfzIDULqPeH5zc1tiqdlA

