package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.VacunaAplicada;
import com.vetcare.modelo.enums.EstadoRecordatorio;
import com.vetcare.modelo.enums.EstadoVacuna;
import com.vetcare.repositorio.OperacionNoPermitidaException;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IVacunaAplicadaRepositorio;

@DisplayName("Repositorio de t_vacuna_aplicada")
class VacunaAplicadaRepositorioTest extends RepositorioTestBase {

    private IVacunaAplicadaRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new VacunaAplicadaRepositorio(fabrica);
    }

    private VacunaAplicada nueva() {
        VacunaAplicada v = new VacunaAplicada();
        v.setIdMascota(3);
        v.setIdTipoVacuna(2);
        v.setFechaAplicacion(LocalDate.of(2026, 11, 10));
        v.setLote("DHP-8040");
        v.setLaboratorio("MSD Animal Health");
        return v;
    }

    @Test
    @DisplayName("guardar devuelve la vacuna VIGENTE con recordatorio PENDIENTE")
    void guardar() {
        VacunaAplicada guardada = repo.guardar(nueva());

        assertEquals(8, guardada.getIdVacunaAplicada());
        assertEquals(EstadoVacuna.VIGENTE, guardada.getEstado());
        assertEquals(EstadoRecordatorio.PENDIENTE, guardada.getEstadoRecordatorio());
        assertEquals("DHP-8040", guardada.getLote());
        // En MySQL el trigger trg_tva_bi_calcular_proxima_dosis llena fecha_proxima_dosis;
        // H2 no tiene el trigger, por eso aquí no se verifica ese valor.
    }

    @Test
    @DisplayName("guardar falla si la mascota no existe (FK)")
    void guardarMascotaInexistente() {
        VacunaAplicada v = nueva();
        v.setIdMascota(999);
        assertThrows(RepositorioException.class, () -> repo.guardar(v));
    }

    @Test
    void buscarPorId() {
        VacunaAplicada v = repo.buscarPorId(1).orElseThrow();
        assertEquals("RAB-2509", v.getLote());
        assertEquals(LocalDate.of(2026, 10, 20), v.getFechaProximaDosis());
        assertNull(v.getFechaEnvioRecordatorio());
    }

    @Test
    void listarTodos() {
        assertEquals(7, repo.listarTodos().size());
    }

    @Test
    @DisplayName("actualizar corrige lote y laboratorio")
    void actualizar() {
        VacunaAplicada v = repo.buscarPorId(3).orElseThrow();
        v.setLote("FEL-3321");
        v.setLaboratorio("Zoetis");
        assertTrue(repo.actualizar(v));

        VacunaAplicada leida = repo.buscarPorId(3).orElseThrow();
        assertEquals("FEL-3321", leida.getLote());
        assertEquals("Zoetis", leida.getLaboratorio());
    }

    @Test
    @DisplayName("eliminar está prohibido (trazabilidad médico-legal)")
    void eliminarNoPermitido() {
        assertThrows(OperacionNoPermitidaException.class, () -> repo.eliminar(1));
        assertEquals(7, repo.contar());
    }

    @Test
    void listarPorMascota() {
        List<VacunaAplicada> deRocky = repo.listarPorMascota(3);
        assertEquals(2, deRocky.size());
        assertEquals(1, repo.listarVigentesPorMascota(3).size(), "Una de Rocky está ANULADA");
    }

    @Test
    @DisplayName("anular deja la vacuna ANULADA con recordatorio NO_APLICA")
    void anular() {
        assertTrue(repo.anular(4));
        VacunaAplicada v = repo.buscarPorId(4).orElseThrow();
        assertEquals(EstadoVacuna.ANULADA, v.getEstado());
        assertEquals(EstadoRecordatorio.NO_APLICA, v.getEstadoRecordatorio());
        assertFalse(repo.anular(4), "Ya estaba anulada");
    }

    @Test
    void actualizarEstadoRecordatorio() {
        LocalDateTime envio = LocalDateTime.of(2026, 11, 17, 8, 0);
        assertTrue(repo.actualizarEstadoRecordatorio(1, EstadoRecordatorio.ENVIADO, envio));

        VacunaAplicada v = repo.buscarPorId(1).orElseThrow();
        assertEquals(EstadoRecordatorio.ENVIADO, v.getEstadoRecordatorio());
        assertEquals(envio, v.getFechaEnvioRecordatorio());
    }

    @Test
    @DisplayName("recordatorios pendientes: VIGENTES, PENDIENTE o FALLIDO, hasta la fecha")
    void listarRecordatoriosPendientes() {
        List<VacunaAplicada> pendientes = repo.listarRecordatoriosPendientes(LocalDate.of(2026, 12, 31));

        assertEquals(3, pendientes.size());
        assertEquals(6, pendientes.get(0).getIdVacunaAplicada()); // FALLIDO, 2026-09-05
        assertEquals(3, pendientes.get(1).getIdVacunaAplicada()); // 2026-10-16
        assertEquals(1, pendientes.get(2).getIdVacunaAplicada()); // 2026-10-20
    }
}
