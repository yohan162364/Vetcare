package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.Mascota;
import com.vetcare.modelo.enums.EstadoMascota;
import com.vetcare.repositorio.OperacionNoPermitidaException;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IMascotaRepositorio;

@DisplayName("Repositorio de t_mascota")
class MascotaRepositorioTest extends RepositorioTestBase {

    private IMascotaRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new MascotaRepositorio(fabrica);
    }

    private Mascota nuevaMascota() {
        Mascota m = new Mascota();
        m.setNombre("Kira");
        m.setFechaNacimiento(LocalDate.of(2024, 2, 14));
        m.setPesoActual(new BigDecimal("6.40"));
        m.setIdGenero(2);
        m.setIdTipoMascota(1);
        m.setIdUsuario(6);
        return m;
    }

    @Test
    @DisplayName("guardar asigna ID y nace ACTIVA")
    void guardar() {
        Mascota guardada = repo.guardar(nuevaMascota());

        assertEquals(6, guardada.getIdMascota());
        assertEquals(EstadoMascota.ACTIVA, guardada.getEstado());
        assertEquals(0, new BigDecimal("6.40").compareTo(guardada.getPesoActual()));
        assertEquals(LocalDate.of(2024, 2, 14), guardada.getFechaNacimiento());
        assertNull(guardada.getFotografia());
    }

    @Test
    @DisplayName("guardar falla si el propietario no existe (FK)")
    void guardarPropietarioInexistente() {
        Mascota m = nuevaMascota();
        m.setIdUsuario(999);
        assertThrows(RepositorioException.class, () -> repo.guardar(m));
    }

    @Test
    @DisplayName("guardar falla con peso negativo (CHECK ck_tm_peso)")
    void guardarPesoInvalido() {
        Mascota m = nuevaMascota();
        m.setPesoActual(new BigDecimal("-1.00"));
        assertThrows(RepositorioException.class, () -> repo.guardar(m));
    }

    @Test
    void buscarPorId() {
        Mascota max = repo.buscarPorId(1).orElseThrow();
        assertEquals("Max", max.getNombre());
        assertEquals(4, max.getIdUsuario());
        assertEquals("img/mascotas/max.jpg", max.getFotografia());
        assertTrue(repo.buscarPorId(77).isEmpty());
    }

    @Test
    void listarTodos() {
        assertEquals(5, repo.listarTodos().size());
    }

    @Test
    @DisplayName("actualizar cambia el peso (HU-008)")
    void actualizar() {
        Mascota max = repo.buscarPorId(1).orElseThrow();
        max.setPesoActual(new BigDecimal("29.10"));
        assertTrue(repo.actualizar(max));
        assertEquals(0, new BigDecimal("29.10").compareTo(repo.buscarPorId(1).orElseThrow().getPesoActual()));
    }

    @Test
    @DisplayName("eliminar está prohibido (trazabilidad médico-legal)")
    void eliminarNoPermitido() {
        assertThrows(OperacionNoPermitidaException.class, () -> repo.eliminar(1));
        assertTrue(repo.existePorId(1));
    }

    @Test
    void listarPorPropietario() {
        List<Mascota> deAna = repo.listarPorPropietario(4);
        assertEquals(2, deAna.size());
        assertEquals("Luna", deAna.get(0).getNombre()); // ordenado por nombre
    }

    @Test
    void listarPorEstado() {
        assertEquals(4, repo.listarPorEstado(EstadoMascota.ACTIVA).size());
        assertEquals(1, repo.listarPorEstado(EstadoMascota.INACTIVA).size());
    }

    @Test
    void buscarPorNombre() {
        assertEquals(1, repo.buscarPorNombre("Roc").size());
        assertEquals(0, repo.buscarPorNombre("Zeus").size());
    }

    @Test
    @DisplayName("inactivar solo cambia mascotas ACTIVAS")
    void inactivar() {
        assertTrue(repo.inactivar(3));
        assertEquals(EstadoMascota.INACTIVA, repo.buscarPorId(3).orElseThrow().getEstado());
        assertFalse(repo.inactivar(3), "Ya estaba inactiva");
        assertFalse(repo.inactivar(5), "Toby ya estaba inactivo");
    }
}
