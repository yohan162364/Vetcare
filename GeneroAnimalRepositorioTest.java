package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.GeneroAnimal;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IGeneroAnimalRepositorio;

@DisplayName("Repositorio de t_genero_animal")
class GeneroAnimalRepositorioTest extends RepositorioTestBase {

    private IGeneroAnimalRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new GeneroAnimalRepositorio(fabrica);
    }

    @Test
    void guardar() {
        GeneroAnimal g = new GeneroAnimal();
        g.setNombreGenero("Intersexual");

        GeneroAnimal guardado = repo.guardar(g);

        assertEquals(4, guardado.getIdGenero());
        assertEquals("Intersexual", guardado.getNombreGenero());
    }

    @Test
    @DisplayName("guardar rechaza nombre repetido (UNIQUE)")
    void guardarDuplicado() {
        GeneroAnimal g = new GeneroAnimal();
        g.setNombreGenero("Macho");
        assertThrows(RepositorioException.class, () -> repo.guardar(g));
    }

    @Test
    void buscarPorId() {
        assertEquals("Hembra", repo.buscarPorId(2).orElseThrow().getNombreGenero());
        assertTrue(repo.buscarPorId(50).isEmpty());
    }

    @Test
    void listarTodos() {
        assertEquals(3, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        GeneroAnimal g = repo.buscarPorId(3).orElseThrow();
        g.setNombreGenero("Sin definir");
        assertTrue(repo.actualizar(g));
        assertEquals("Sin definir", repo.buscarPorId(3).orElseThrow().getNombreGenero());
    }

    @Test
    @DisplayName("eliminar un género sin mascotas funciona; con mascotas lo impide la FK")
    void eliminar() {
        assertTrue(repo.eliminar(3));
        assertEquals(2, repo.contar());
        assertThrows(RepositorioException.class, () -> repo.eliminar(1));
    }

    @Test
    void buscarPorNombre() {
        assertEquals(1, repo.buscarPorNombre("Macho").orElseThrow().getIdGenero());
        assertTrue(repo.buscarPorNombre("Otro").isEmpty());
    }
}
