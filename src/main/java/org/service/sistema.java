package org.service;

public interface sistema {
    // Funciones generales
    void iniciarSesion();
    void registrarCliente(String email);
    void abrirCuentaBancaria(String email);

    // Funciones de usuario
    void mostrarOpcionesCliente(String email);
    void realizarOperacion(String email);
    void realizarDeposito();
    void realizarRetiro();
    void realizarTransferencia();
    void consultarSaldo(String email);

    // Funciones de administrador
    void mostrarOpcionesAdministrador();
    void consultarHistorialTransacciones();
    void generarReporteFinanciero();
    void verCuentasInactivas();
    void configurarUsuarios();
}