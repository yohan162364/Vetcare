package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.AntecedentePoliza;
import com.vetcare.modelo.AntecedentePolizaId;
import com.vetcare.modelo.enums.CondicionCobertura;
import com.vetcare.modelo.enums.EstadoValidacion;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IAntecedentePolizaRepositorio;

@DisplayName("Repositorio de t_antecedente_poliza")
class AntecedentePolizaRepositorioTest extends RepositorioTestBase {

    private IAntecedentePolizaRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new AntecedentePolizaRepositorio(fabrica);
    }

    @Test
    @DisplayName("guardar usa PREEXISTENCIA y PENDIENTE por defecto")
    void guardar() {
        AntecedentePoliza ap = new AntecedentePoliza();
        ap.setIdAntecedente(5);
        ap.setIdPoliza(1);

        AntecedentePoliza guardado = repo.guardar(ap);

        assertEquals(new AntecedentePolizaId(5, 1), guardado.getId());
        assertEquals(CondicionCobertura.PREEXISTENCIA, guardado.getCondicionCobertura());
        assertEquals(EstadoValidacion.PENDIENTE, guardado.getEstadoValidacion());
        assertNull(guardado.getFechaValidacion());
    }

    @Test
    @DisplayName("guardar falla si la póliza no existe (FK)")
    void guardarPolizaInexistente() {
        AntecedentePoliza ap = new AntecedentePoliza();
        ap.setIdAntecedente(1);
        ap.setIdPoliza(99);
        assertThrows(RepositorioException.class, () -> repo.guardar(ap));
    }

    @Test
    void buscarPorId() {
        AntecedentePoliza ap = repo.buscarPorId(new AntecedentePolizaId(3, 2)).orElseThrow();
        assertEquals(CondicionCobertura.CUBIERTO, ap.getCondicionCobertura());
        assertEquals(LocalDate.of(2025, 7, 1), ap.getFechaValidacion());
    }

    @Test
    void listarTodos() {
        assertEquals(3, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        AntecedentePoliza ap = repo.buscarPorId(new AntecedentePolizaId(1, 3)).orElseThrow();
        ap.setCondicionCobertura(CondicionCobertura.CUBIERTO);
        assertTrue(repo.actualizar(ap));
        assertEquals(CondicionCobertura.CUBIERTO,
                repo.buscarPorId(new AntecedentePolizaId(1, 3)).orElseThrow().getCondicionCobertura());
    }

    @Test
    void eliminar() {
        assertTrue(repo.eliminar(new AntecedentePolizaId(1, 3)));
        assertEquals(2, repo.contar());
    }

    @Test
    void listarPorPolizaYAntecedente() {
        assertEquals(1, repo.listarPorPoliza(1).size());
        assertEquals(1, repo.listarPorAntecedente(3).size());
    }

    @Test
    void cambiarEstadoValidacion() {
        AntecedentePolizaId id = new AntecedentePolizaId(1, 3);
        LocalDate hoy = LocalDate.of(2026, 11, 17);
        assertTrue(repo.cambiarEstadoValidacion(id, EstadoValidacion.RECHAZADA, hoy));

        AntecedentePoliza ap = repo.buscarPorId(id).orElseThrow();
        assertEquals(EstadoValidacion.RECHAZADA, ap.getEstadoValidacion());
        assertEquals(hoy, ap.getFechaValidacion());
    }
}
