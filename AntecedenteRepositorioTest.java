package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.Antecedente;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IAntecedenteRepositorio;

@DisplayName("Repositorio de t_antecedente")
class AntecedenteRepositorioTest extends RepositorioTestBase {

    private IAntecedenteRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new AntecedenteRepositorio(fabrica);
    }

    private Antecedente nuevo(String enfermedad) {
        Antecedente a = new Antecedente();
        a.setEnfermedad(enfermedad);
        return a;
    }

    @Test
    @DisplayName("guardar asigna ID y fecha de registro")
    void guardar() {
        Antecedente guardado = repo.guardar(nuevo("Diabetes mellitus"));
        assertEquals(6, guardado.getIdAntecedente());
        assertNotNull(guardado.getFechaRegistro());
    }

    @Test
    void guardarDuplicado() {
        assertThrows(RepositorioException.class, () -> repo.guardar(nuevo("Otitis externa")));
    }

    @Test
    void buscarPorId() {
        assertEquals("Displasia de cadera", repo.buscarPorId(2).orElseThrow().getEnfermedad());
    }

    @Test
    void listarTodos() {
        assertEquals(5, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        Antecedente a = repo.buscarPorId(5).orElseThrow();
        a.setEnfermedad("Gastroenteritis aguda");
        assertTrue(repo.actualizar(a));
        assertEquals("Gastroenteritis aguda", repo.buscarPorId(5).orElseThrow().getEnfermedad());
    }

    @Test
    @DisplayName("eliminar funciona si no está en uso; si una mascota lo tiene lo impide la FK")
    void eliminar() {
        assertTrue(repo.eliminar(5));                                      // Gastroenteritis: sin uso
        assertThrows(RepositorioException.class, () -> repo.eliminar(2));  // lo tiene Max
    }

    @Test
    void buscarPorEnfermedad() {
        assertEquals(4, repo.buscarPorEnfermedad("Insuficiencia renal crónica").orElseThrow().getIdAntecedente());
        assertTrue(repo.buscarPorEnfermedad("Moquillo").isEmpty());
    }
}
