package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.TipoVacuna;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.ITipoVacunaRepositorio;

@DisplayName("Repositorio de t_tipo_vacuna")
class TipoVacunaRepositorioTest extends RepositorioTestBase {

    private ITipoVacunaRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new TipoVacunaRepositorio(fabrica);
    }

    private TipoVacuna nueva(String nombre, int intervalo) {
        TipoVacuna t = new TipoVacuna();
        t.setNombreVacuna(nombre);
        t.setEsquemaBase("Dosis anual");
        t.setIntervaloRecomendadoDias(intervalo);
        return t;
    }

    @Test
    void guardar() {
        TipoVacuna guardada = repo.guardar(nueva("Leptospirosis", 365));
        assertEquals(5, guardada.getIdTipoVacuna());
        assertEquals(365, guardada.getIntervaloRecomendadoDias());
    }

    @Test
    @DisplayName("guardar rechaza intervalo 0 (CHECK ck_ttv_intervalo)")
    void guardarIntervaloInvalido() {
        assertThrows(RepositorioException.class, () -> repo.guardar(nueva("Inválida", 0)));
    }

    @Test
    void buscarPorId() {
        TipoVacuna t = repo.buscarPorId(4).orElseThrow();
        assertEquals("Bordetella (tos de perreras)", t.getNombreVacuna());
        assertEquals(180, t.getIntervaloRecomendadoDias());
    }

    @Test
    void listarTodos() {
        assertEquals(4, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        TipoVacuna t = repo.buscarPorId(4).orElseThrow();
        t.setIntervaloRecomendadoDias(365);
        assertTrue(repo.actualizar(t));
        assertEquals(365, repo.buscarPorId(4).orElseThrow().getIntervaloRecomendadoDias());
    }

    @Test
    void eliminar() {
        TipoVacuna nueva = repo.guardar(nueva("Temporal", 30));
        assertTrue(repo.eliminar(nueva.getIdTipoVacuna()));
        assertThrows(RepositorioException.class, () -> repo.eliminar(1)); // tiene vacunas aplicadas
    }

    @Test
    void buscarPorNombre() {
        assertEquals(1, repo.buscarPorNombre("Antirrábica").orElseThrow().getIdTipoVacuna());
        assertTrue(repo.buscarPorNombre("Parvovirus").isEmpty());
    }
}
