package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.TipoMascota;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.ITipoMascotaRepositorio;

@DisplayName("Repositorio de t_tipo_mascota")
class TipoMascotaRepositorioTest extends RepositorioTestBase {

    private ITipoMascotaRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new TipoMascotaRepositorio(fabrica);
    }

    private TipoMascota tipo(String especie, String raza) {
        TipoMascota t = new TipoMascota();
        t.setEspecie(especie);
        t.setRaza(raza);
        return t;
    }

    @Test
    void guardar() {
        TipoMascota guardado = repo.guardar(tipo("Canino", "Golden Retriever"));
        assertEquals(6, guardado.getIdTipoMascota());
        assertEquals("Golden Retriever", guardado.getRaza());
    }

    @Test
    @DisplayName("guardar rechaza la misma especie y raza (UNIQUE compuesto)")
    void guardarDuplicado() {
        assertThrows(RepositorioException.class, () -> repo.guardar(tipo("Felino", "Siamés")));
    }

    @Test
    void buscarPorId() {
        TipoMascota t = repo.buscarPorId(2).orElseThrow();
        assertEquals("Canino", t.getEspecie());
        assertEquals("Labrador Retriever", t.getRaza());
    }

    @Test
    void listarTodos() {
        assertEquals(5, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        TipoMascota t = repo.buscarPorId(3).orElseThrow();
        t.setRaza("Bulldog Inglés");
        assertTrue(repo.actualizar(t));
        assertEquals("Bulldog Inglés", repo.buscarPorId(3).orElseThrow().getRaza());
    }

    @Test
    void eliminar() {
        TipoMascota nuevo = repo.guardar(tipo("Ave", "Canario"));
        assertTrue(repo.eliminar(nuevo.getIdTipoMascota()));
        assertThrows(RepositorioException.class, () -> repo.eliminar(2)); // lo usa Max
    }

    @Test
    void listarPorEspecie() {
        List<TipoMascota> caninos = repo.listarPorEspecie("Canino");
        assertEquals(3, caninos.size());
        assertEquals(2, repo.listarPorEspecie("Felino").size());
    }

    @Test
    void buscarPorEspecieYRaza() {
        assertEquals(4, repo.buscarPorEspecieYRaza("Felino", "Criollo").orElseThrow().getIdTipoMascota());
        assertTrue(repo.buscarPorEspecieYRaza("Felino", "Persa").isEmpty());
    }
}
