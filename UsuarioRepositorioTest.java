package com.vetcare.repositorio.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vetcare.modelo.Usuario;
import com.vetcare.modelo.enums.EstadoUsuario;
import com.vetcare.modelo.enums.Rol;
import com.vetcare.repositorio.RepositorioException;
import com.vetcare.repositorio.RepositorioTestBase;
import com.vetcare.repositorio.interfaces.IUsuarioRepositorio;

@DisplayName("Repositorio de t_usuario")
class UsuarioRepositorioTest extends RepositorioTestBase {

    private IUsuarioRepositorio repo;

    @BeforeEach
    void setUp() {
        repo = new UsuarioRepositorio(fabrica);
    }

    private Usuario nuevoUsuario(String username, String documento, String correo) {
        Usuario u = new Usuario();
        u.setNombre("Pedro Prueba");
        u.setDocumentoIdentidad(documento);
        u.setUsername(username);
        u.setContrasenaHash("$2a$10$hashDePrueba");
        u.setCorreo(correo);
        u.setTelefono("3001112233");
        u.setDireccion("Cl. 1 # 2-3");
        return u;
    }

    @Test
    @DisplayName("guardar asigna ID, rol PROPIETARIO, estado ACTIVO y fecha de registro")
    void guardar() {
        Usuario guardado = repo.guardar(nuevoUsuario("pprueba", "1099999999", "pedro@correo.com"));

        assertNotNull(guardado.getIdUsuario());
        assertEquals(7, guardado.getIdUsuario());
        assertEquals(Rol.PROPIETARIO, guardado.getRol());
        assertEquals(EstadoUsuario.ACTIVO, guardado.getEstado());
        assertNotNull(guardado.getFechaRegistro(), "La BD asigna fecha_registro");
        assertEquals(7, repo.contar());
    }

    @Test
    @DisplayName("guardar rechaza username repetido (UNIQUE)")
    void guardarUsernameDuplicado() {
        assertThrows(RepositorioException.class,
                () -> repo.guardar(nuevoUsuario("lgomez", "1099999999", "otro@correo.com")));
    }

    @Test
    void buscarPorId() {
        Optional<Usuario> u = repo.buscarPorId(2);
        assertTrue(u.isPresent());
        assertEquals("crodriguez", u.get().getUsername());
        assertEquals(Rol.VETERINARIO, u.get().getRol());
        assertTrue(repo.buscarPorId(999).isEmpty());
    }

    @Test
    void listarTodos() {
        List<Usuario> todos = repo.listarTodos();
        assertEquals(6, todos.size());
        assertEquals(1, todos.get(0).getIdUsuario());
    }

    @Test
    @DisplayName("actualizar modifica los datos pero no la contraseña")
    void actualizar() {
        Usuario u = repo.buscarPorId(4).orElseThrow();
        String hashOriginal = u.getContrasenaHash();
        u.setTelefono("3200000000");
        u.setContrasenaHash("no-debe-guardarse");

        assertTrue(repo.actualizar(u));

        Usuario leido = repo.buscarPorId(4).orElseThrow();
        assertEquals("3200000000", leido.getTelefono());
        assertEquals(hashOriginal, leido.getContrasenaHash());
    }

    @Test
    void actualizarContrasena() {
        assertTrue(repo.actualizarContrasena(4, "$2a$10$nuevoHash"));
        assertEquals("$2a$10$nuevoHash", repo.buscarPorId(4).orElseThrow().getContrasenaHash());
    }

    @Test
    @DisplayName("eliminar funciona sin mascotas y falla por FK si tiene mascotas")
    void eliminar() {
        assertTrue(repo.eliminar(1));                    // administradora, sin mascotas
        assertFalse(repo.existePorId(1));
        assertThrows(RepositorioException.class, () -> repo.eliminar(4)); // propietaria de Max y Luna
    }

    @Test
    void buscarPorUsernameCorreoYDocumento() {
        assertEquals(4, repo.buscarPorUsername("amartinez").orElseThrow().getIdUsuario());
        assertEquals(5, repo.buscarPorCorreo("julian.perez@correo.com").orElseThrow().getIdUsuario());
        assertEquals(6, repo.buscarPorDocumento("1067890123").orElseThrow().getIdUsuario());
        assertTrue(repo.buscarPorUsername("noexiste").isEmpty());
    }

    @Test
    void listarPorRol() {
        assertEquals(3, repo.listarPorRol(Rol.PROPIETARIO).size());
        assertEquals(2, repo.listarPorRol(Rol.VETERINARIO).size());
        assertEquals(1, repo.listarPorRol(Rol.ADMINISTRADOR).size());
    }

    @Test
    void cambiarEstado() {
        assertTrue(repo.cambiarEstado(5, EstadoUsuario.INACTIVO));
        assertEquals(EstadoUsuario.INACTIVO, repo.buscarPorId(5).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("toString no expone el hash de la contraseña")
    void toStringSinHash() {
        Usuario u = repo.buscarPorId(1).orElseThrow();
        assertFalse(u.toString().contains(u.getContrasenaHash()));
    }
}
