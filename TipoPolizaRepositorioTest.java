package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.TipoPoliza;
import com.vetcare.modelo.enums.ModalidadAtencion;
import com.vetcare.modelo.enums.NivelCobertura;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.ITipoPolizaRepositorio;

@DisplayName("Repositorio de t_tipo_poliza")
class TipoPolizaRepositorioTest extends RepositorioTestBase {

    private ITipoPolizaRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new TipoPolizaRepositorio(fabrica);
    }

    private TipoPoliza nueva(String nombre, String tope) {
        TipoPoliza t = new TipoPoliza();
        t.setNombrePoliza(nombre);
        t.setAseguradora("Aseguradora de Prueba");
        t.setModalidadAtencion(ModalidadAtencion.REEMBOLSO);
        t.setTopeAnualCobertura(new BigDecimal(tope));
        t.setCondiciones("Carencia de 30 días.");
        t.setCoberturas("Consultas y urgencias.");
        return t;
    }

    @Test
    @DisplayName("guardar usa nivel BASICO por defecto")
    void guardar() {
        TipoPoliza guardada = repo.guardar(nueva("Mascota Senior", "4000000.00"));
        assertEquals(4, guardada.getIdTipoPoliza());
        assertEquals(NivelCobertura.BASICO, guardada.getNivelCobertura());
        assertEquals(ModalidadAtencion.REEMBOLSO, guardada.getModalidadAtencion());
        assertEquals(0, new BigDecimal("4000000.00").compareTo(guardada.getTopeAnualCobertura()));
    }

    @Test
    @DisplayName("guardar rechaza tope 0 (CHECK ck_ttp_tope)")
    void guardarTopeInvalido() {
        assertThrows(RepositorioException.class, () -> repo.guardar(nueva("Sin tope", "0")));
    }

    @Test
    void buscarPorId() {
        TipoPoliza t = repo.buscarPorId(3).orElseThrow();
        assertEquals("Mascota Premium", t.getNombrePoliza());
        assertEquals(NivelCobertura.PREMIUM, t.getNivelCobertura());
    }

    @Test
    void listarTodos() {
        assertEquals(3, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        TipoPoliza t = repo.buscarPorId(1).orElseThrow();
        t.setTopeAnualCobertura(new BigDecimal("3000000.00"));
        assertTrue(repo.actualizar(t));
        assertEquals(0, new BigDecimal("3000000.00")
                .compareTo(repo.buscarPorId(1).orElseThrow().getTopeAnualCobertura()));
    }

    @Test
    void eliminar() {
        TipoPoliza nueva = repo.guardar(nueva("Temporal", "100000.00"));
        assertTrue(repo.eliminar(nueva.getIdTipoPoliza()));
        assertThrows(RepositorioException.class, () -> repo.eliminar(1)); // tiene pólizas
    }

    @Test
    void buscarPorNombreYNivel() {
        assertEquals(2, repo.buscarPorNombre("Mascota Plus").orElseThrow().getIdTipoPoliza());
        assertEquals(1, repo.listarPorNivel(NivelCobertura.INTERMEDIO).size());
    }
}
