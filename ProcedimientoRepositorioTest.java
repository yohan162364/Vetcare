package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.Procedimiento;
import com.vetcare.modelo.enums.EstadoRecordatorio;
import com.vetcare.repositorio.OperacionNoPermitidaException;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IProcedimientoRepositorio;

@DisplayName("Repositorio de t_procedimiento")
class ProcedimientoRepositorioTest extends RepositorioTestBase {

    private IProcedimientoRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new ProcedimientoRepositorio(fabrica);
    }

    private Procedimiento nuevo(LocalDate fecha, LocalDate control) {
        Procedimiento p = new Procedimiento();
        p.setDiagnosticoAsociado("Extracción de cuerpo extraño en pata delantera.");
        p.setFechaProcedimiento(fecha);
        p.setFechaProximoControl(control);
        return p;
    }

    @Test
    void guardar() {
        Procedimiento guardado = repo.guardar(nuevo(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 18)));
        assertEquals(4, guardado.getIdProcedimiento());
        assertEquals(LocalDate.of(2026, 11, 18), guardado.getFechaProximoControl());
        assertNotNull(guardado.getEstadoRecordatorio());
        // En MySQL el trigger trg_tpr_bi_estado_recordatorio lo deja en PENDIENTE (tiene control).
    }

    @Test
    @DisplayName("guardar rechaza control anterior al procedimiento (CHECK ck_tpr_control)")
    void guardarControlInvalido() {
        assertThrows(RepositorioException.class,
                () -> repo.guardar(nuevo(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1))));
    }

    @Test
    void buscarPorId() {
        Procedimiento p = repo.buscarPorId(3).orElseThrow();
        assertNull(p.getFechaProximoControl());
        assertEquals(EstadoRecordatorio.NO_APLICA, p.getEstadoRecordatorio());
    }

    @Test
    void listarTodos() {
        assertEquals(3, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        Procedimiento p = repo.buscarPorId(1).orElseThrow();
        p.setFechaProximoControl(LocalDate.of(2026, 10, 30));
        assertTrue(repo.actualizar(p));
        assertEquals(LocalDate.of(2026, 10, 30), repo.buscarPorId(1).orElseThrow().getFechaProximoControl());
    }

    @Test
    @DisplayName("eliminar está prohibido (trazabilidad médico-legal)")
    void eliminarNoPermitido() {
        assertThrows(OperacionNoPermitidaException.class, () -> repo.eliminar(1));
    }

    @Test
    @DisplayName("listarPorMascota une con t_mascota_procedimiento")
    void listarPorMascota() {
        List<Procedimiento> deMax = repo.listarPorMascota(1);
        assertEquals(1, deMax.size());
        assertEquals(1, deMax.get(0).getIdProcedimiento());
        assertTrue(repo.listarPorMascota(3).isEmpty());
    }

    @Test
    void actualizarEstadoRecordatorio() {
        LocalDateTime envio = LocalDateTime.of(2026, 10, 14, 8, 0);
        assertTrue(repo.actualizarEstadoRecordatorio(1, EstadoRecordatorio.ENVIADO, envio));

        Procedimiento p = repo.buscarPorId(1).orElseThrow();
        assertEquals(EstadoRecordatorio.ENVIADO, p.getEstadoRecordatorio());
        assertEquals(envio, p.getFechaEnvioRecordatorio());
    }

    @Test
    void listarRecordatoriosPendientes() {
        List<Procedimiento> pendientes = repo.listarRecordatoriosPendientes(LocalDate.of(2026, 12, 31));
        assertEquals(1, pendientes.size());
        assertEquals(1, pendientes.get(0).getIdProcedimiento());
        assertTrue(repo.listarRecordatoriosPendientes(LocalDate.of(2026, 10, 1)).isEmpty());
    }
}
