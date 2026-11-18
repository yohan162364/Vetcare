package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.Poliza;
import com.vetcare.modelo.enums.EstadoPoliza;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IPolizaRepositorio;

@DisplayName("Repositorio de t_poliza")
class PolizaRepositorioTest extends RepositorioTestBase {

    private IPolizaRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new PolizaRepositorio(fabrica);
    }

    private Poliza nueva(LocalDate inicio, LocalDate fin) {
        Poliza p = new Poliza();
        p.setIdTipoPoliza(1);
        p.setIdMascota(4);
        p.setVigenciaInicio(inicio);
        p.setVigenciaFin(fin);
        return p;
    }

    @Test
    void guardar() {
        Poliza guardada = repo.guardar(nueva(LocalDate.of(2026, 11, 1), LocalDate.of(2027, 10, 31)));
        assertEquals(5, guardada.getIdPoliza());
        assertEquals(EstadoPoliza.ACTIVA, guardada.getEstado());
    }

    @Test
    @DisplayName("guardar rechaza vigencia_fin anterior al inicio (CHECK ck_tpz_vigencia)")
    void guardarVigenciaInvalida() {
        assertThrows(RepositorioException.class,
                () -> repo.guardar(nueva(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 10, 1))));
    }

    @Test
    void buscarPorId() {
        Poliza p = repo.buscarPorId(3).orElseThrow();
        assertEquals(3, p.getIdTipoPoliza());
        assertEquals(LocalDate.of(2027, 2, 28), p.getVigenciaFin());
    }

    @Test
    void listarTodos() {
        assertEquals(4, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        Poliza p = repo.buscarPorId(1).orElseThrow();
        p.setVigenciaFin(LocalDate.of(2027, 1, 31));
        assertTrue(repo.actualizar(p));
        assertEquals(LocalDate.of(2027, 1, 31), repo.buscarPorId(1).orElseThrow().getVigenciaFin());
    }

    @Test
    @DisplayName("eliminar funciona sin antecedentes asociados; con antecedentes lo impide la FK")
    void eliminar() {
        assertTrue(repo.eliminar(4));
        assertThrows(RepositorioException.class, () -> repo.eliminar(1));
    }

    @Test
    void listarPorMascotaYEstado() {
        assertEquals(1, repo.listarPorMascota(1).size());
        assertEquals(3, repo.listarPorEstado(EstadoPoliza.ACTIVA).size());
        assertEquals(1, repo.listarPorEstado(EstadoPoliza.CANCELADA).size());
    }

    @Test
    void listarVigentesPorMascota() {
        LocalDate hoy = LocalDate.of(2026, 11, 17);
        assertEquals(1, repo.listarVigentesPorMascota(1, hoy).size());
        assertEquals(0, repo.listarVigentesPorMascota(2, hoy).size(), "La de Luna terminó en mayo");
    }

    @Test
    void cancelar() {
        assertTrue(repo.cancelar(3));
        assertEquals(EstadoPoliza.CANCELADA, repo.buscarPorId(3).orElseThrow().getEstado());
        assertFalse(repo.cancelar(4), "Ya estaba cancelada");
    }

    @Test
    @DisplayName("marcarVencidas cambia solo las ACTIVAS con vigencia terminada")
    void marcarVencidas() {
        assertEquals(1, repo.marcarVencidas(LocalDate.of(2026, 11, 17)));
        assertEquals(EstadoPoliza.VENCIDA, repo.buscarPorId(2).orElseThrow().getEstado());
        assertEquals(EstadoPoliza.ACTIVA, repo.buscarPorId(1).orElseThrow().getEstado());
    }
}
