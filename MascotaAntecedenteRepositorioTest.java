package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.MascotaAntecedente;
import com.vetcare.modelo.MascotaAntecedenteId;
import com.vetcare.repositorio.OperacionNoPermitidaException;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IMascotaAntecedenteRepositorio;

@DisplayName("Repositorio de t_mascota_antecedente")
class MascotaAntecedenteRepositorioTest extends RepositorioTestBase {

    private IMascotaAntecedenteRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new MascotaAntecedenteRepositorio(fabrica);
    }

    private MascotaAntecedente nuevo(int idMascota, int idAntecedente) {
        MascotaAntecedente ma = new MascotaAntecedente();
        ma.setIdMascota(idMascota);
        ma.setIdAntecedente(idAntecedente);
        ma.setDiagnostico("Diagnóstico de prueba.");
        return ma;
    }

    @Test
    @DisplayName("guardar con llave compuesta y fecha automática")
    void guardar() {
        MascotaAntecedente guardado = repo.guardar(nuevo(4, 5));
        assertEquals(new MascotaAntecedenteId(4, 5), guardado.getId());
        assertNotNull(guardado.getFechaDiagnostico());
        assertEquals(5, repo.contar());
    }

    @Test
    @DisplayName("guardar rechaza la misma pareja mascota–antecedente (PK)")
    void guardarDuplicado() {
        assertThrows(RepositorioException.class, () -> repo.guardar(nuevo(1, 2)));
    }

    @Test
    void buscarPorId() {
        MascotaAntecedente ma = repo.buscarPorId(new MascotaAntecedenteId(1, 2)).orElseThrow();
        assertTrue(ma.getDiagnostico().startsWith("Displasia leve"));
        assertEquals(LocalDateTime.of(2025, 11, 10, 10, 30), ma.getFechaDiagnostico());
        assertTrue(repo.buscarPorId(new MascotaAntecedenteId(1, 1)).isEmpty());
    }

    @Test
    void listarTodos() {
        assertEquals(4, repo.listarTodos().size());
    }

    @Test
    void actualizar() {
        MascotaAntecedente ma = repo.buscarPorId(new MascotaAntecedenteId(2, 3)).orElseThrow();
        ma.setDiagnostico("Otitis resuelta tras tratamiento.");
        assertTrue(repo.actualizar(ma));
        assertEquals("Otitis resuelta tras tratamiento.",
                repo.buscarPorId(new MascotaAntecedenteId(2, 3)).orElseThrow().getDiagnostico());
    }

    @Test
    @DisplayName("eliminar está prohibido (trazabilidad médico-legal)")
    void eliminarNoPermitido() {
        assertThrows(OperacionNoPermitidaException.class, () -> repo.eliminar(new MascotaAntecedenteId(1, 2)));
    }

    @Test
    void listarPorMascotaYAntecedente() {
        assertEquals(1, repo.listarPorMascota(1).size());
        assertEquals(1, repo.listarPorAntecedente(4).size());
        assertEquals(0, repo.listarPorMascota(4).size());
    }

    @Test
    void actualizarDiagnostico() {
        MascotaAntecedenteId id = new MascotaAntecedenteId(3, 1);
        assertTrue(repo.actualizarDiagnostico(id, "Dermatitis controlada."));
        assertEquals("Dermatitis controlada.", repo.buscarPorId(id).orElseThrow().getDiagnostico());
    }
}
