package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.MascotaProcedimiento;
import com.vetcare.modelo.MascotaProcedimientoId;
import com.vetcare.repositorio.OperacionNoPermitidaException;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IMascotaProcedimientoRepositorio;

@DisplayName("Repositorio de t_mascota_procedimiento")
class MascotaProcedimientoRepositorioTest extends RepositorioTestBase {

    private IMascotaProcedimientoRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new MascotaProcedimientoRepositorio(fabrica);
    }

    private MascotaProcedimiento vinculo(int idMascota, int idProcedimiento) {
        MascotaProcedimiento mp = new MascotaProcedimiento();
        mp.setIdMascota(idMascota);
        mp.setIdProcedimiento(idProcedimiento);
        return mp;
    }

    @Test
    @DisplayName("guardar vincula un procedimiento a otra mascota (N:M)")
    void guardar() {
        MascotaProcedimiento guardado = repo.guardar(vinculo(3, 1));
        assertEquals(new MascotaProcedimientoId(3, 1), guardado.getId());
        assertEquals(2, repo.listarPorProcedimiento(1).size());
    }

    @Test
    void guardarDuplicadoOFkInvalida() {
        assertThrows(RepositorioException.class, () -> repo.guardar(vinculo(1, 1)));  // PK repetida
        assertThrows(RepositorioException.class, () -> repo.guardar(vinculo(1, 99))); // FK inexistente
    }

    @Test
    void buscarPorId() {
        assertTrue(repo.buscarPorId(new MascotaProcedimientoId(4, 2)).isPresent());
        assertTrue(repo.buscarPorId(new MascotaProcedimientoId(4, 1)).isEmpty());
        assertTrue(repo.existePorId(new MascotaProcedimientoId(2, 3)));
    }

    @Test
    void listarTodos() {
        assertEquals(3, repo.listarTodos().size());
    }

    @Test
    @DisplayName("actualizar no aplica: la tabla solo tiene la llave")
    void actualizarNoPermitido() {
        assertThrows(OperacionNoPermitidaException.class, () -> repo.actualizar(vinculo(1, 1)));
    }

    @Test
    @DisplayName("eliminar está prohibido (trazabilidad médico-legal)")
    void eliminarNoPermitido() {
        assertThrows(OperacionNoPermitidaException.class, () -> repo.eliminar(new MascotaProcedimientoId(1, 1)));
    }

    @Test
    void listarPorMascota() {
        assertEquals(1, repo.listarPorMascota(4).size());
        assertEquals(0, repo.listarPorMascota(5).size());
    }
}
