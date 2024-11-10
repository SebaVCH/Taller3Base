package org.service;

import org.db.DatabaseConnection;
import org.model.Cliente;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class sistemaIMPL implements sistema{

    //Scanner

    Scanner sc = new Scanner(System.in);

    @Override
    public void iniciarSesion() {

        System.out.println("Bienvenido a BancaSegura \n Por favor indique su correo: ");
        String email = sc.nextLine();
        System.out.println("Ahora indique su contraseña: ");
        String password = sc.nextLine();

        Cliente cliente = obtenerClientePorMail(email);

        if(email.equals("skibidi") && password.equals("toilet")) {
            mostrarOpcionesAdministrador();
        } else if (cliente == null) {
            System.out.println("No existe ningun cliente asociado a este correo. Deseas crear una cuenta? (selecciona una opcion numerica) ");
            System.out.println("1)SI");
            System.out.println("2)NO (Salir del banco)");
            System.out.println("3)Reintentar inicio de sesion");
            System.out.print(">> ");
            int opcion = Integer.parseInt(sc.nextLine());

            switch (opcion) {
                case 1:
                    registrarCliente(email);
                    break;
                case 2:
                    System.out.println("Saliendo del banco...");
                    break;
                case 3:
                    iniciarSesion();
                    break;
                default:
                    System.out.println("Opcion invalida");
            }

        } else if (!password.equals(cliente.getPassword())){
            System.out.println("Contraseña incorrecta");
        } else {
            mostrarOpcionesCliente(email);
        }

    }

    private Cliente obtenerClientePorMail(String email) {
        Cliente cliente = null;

        DatabaseConnection dbConnection = new DatabaseConnection();

        try {
            if (dbConnection.getConnection() != null) {

                Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM cliente WHERE email = '" + email + "'");
                if (rs.next()){
                    cliente = new Cliente(rs.getString("email"), rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por email");
        }
    
        return cliente;
    }

    @Override
    public void registrarCliente(String email) {
    
        System.out.println("Por favor ingrese lo siguientes datos:");
        System.out.println("Nombre completo: ");
        String nombreCompleto = sc.nextLine();
        System.out.println("Dirección: ");
        String direccion = sc.nextLine();
        System.out.println("Telefono: ");
        String telefono = sc.nextLine();
        System.out.println("Contraseña: ");
        String password = sc.nextLine();
    
        Cliente cliente = new Cliente(email, password);
        cliente.setNombreCompleto(nombreCompleto);
        cliente.setDireccion(direccion);
        cliente.setTelefono(telefono);
        registrarClienteEnBaseDeDatos(cliente);

        abrirCuentaBancaria(email);
    }
    
    private void registrarClienteEnBaseDeDatos(Cliente cliente) {
        DatabaseConnection dbConnection = new DatabaseConnection();
    
        try {
            if (dbConnection.getConnection() != null) {
                PreparedStatement stmt = dbConnection.getConnection().prepareStatement(
                        "INSERT INTO cliente (email, password, nombreCompleto, direccion, telefono) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, cliente.getEmail());
                stmt.setString(2, cliente.getPassword());
                stmt.setString(3, cliente.getNombreCompleto());
                stmt.setString(4, cliente.getDireccion());
                stmt.setString(5, cliente.getTelefono());
    
                stmt.executeUpdate();
                System.out.println("Cliente registrado correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar cliente en la base de datos");
        }
    }

    @Override
    public void abrirCuentaBancaria(String email) {

        DatabaseConnection dbConnection = new DatabaseConnection();

        try {
            if (dbConnection.getConnection() != null) {
                PreparedStatement stmt = dbConnection.getConnection().prepareStatement(
                        "INSERT INTO cuentabancaria (email,fechaCreacion) VALUES (?, ?)");
                stmt.setString(1, email);
                stmt.setDate(2,java.sql.Date.valueOf(LocalDate.now()));

                stmt.executeUpdate();
                System.out.println("Cuenta bancaria creada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar cliente en la base de datos");
        }

    }

    @Override
    public void mostrarOpcionesCliente(String email) {

        System.out.println("Bienvenido! Seleccione una opcion:");
        System.out.println("\n 1) Realizar una operacion \n 2) Abrir una cuenta nueva: \n 3) Consultar Saldo:");
        int opcion = Integer.parseInt(sc.nextLine());
        switch (opcion){
            case 1:
                realizarOperacion(email);
                break;
            case 2:
                abrirCuentaBancaria(email);
                break;
            case 3:
                consultarSaldo(email);
                break;
            default:
                System.out.println("Ingrese una opcion valida...");
                break;
        }

    }

    @Override
    public void realizarOperacion(String email) {
        System.out.println("Seleccione el tipo de operación que desea realizar:");
        System.out.println("1) Depósito");
        System.out.println("2) Retiro");
        System.out.println("3) Transferencia");
        System.out.print(">> ");
    
        int opcion = Integer.parseInt(sc.nextLine());
    
        switch (opcion) {
            case 1:
                realizarDeposito(email);
                break;
            case 2:
                realizarRetiro(email);
                break;
            case 3:
                realizarTransferencia(email);
                break;
            default:
                System.out.println("Opción inválida, intente de nuevo.");
                realizarOperacion(email); 
        }
    }

    @Override
    public void realizarDeposito() {

        System.out.println("Ingrese el ID de la cuenta para realizar el depósito:");
        int idCuenta = Integer.parseInt(sc.nextLine());
        System.out.println("Ingrese el monto a depositar:");
        int monto = Integer.parseInt(sc.nextLine());

        if (monto <= 0) {
            System.out.println("El monto debe ser positivo.");
            return;
        }

        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn != null) {
                
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE CuentaBancaria SET saldo = saldo + ? WHERE idCuenta = ?");
                stmt.setInt(1, monto);
                stmt.setInt(2, idCuenta);
                stmt.executeUpdate();

                
                PreparedStatement operacionStmt = conn.prepareStatement(
                        "INSERT INTO Operacion (idCuentaOrigen, tipoOperacion, monto, fechaOperacion) " +
                        "VALUES (?, 'DEPOSITO', ?, current_date)");
                operacionStmt.setInt(1, idCuenta);
                operacionStmt.setInt(2, monto);
                operacionStmt.executeUpdate();

                System.out.println("Depósito realizado exitosamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al realizar el depósito: " + e.getMessage());
        }

    }

    @Override
    public void realizarRetiro() {
        System.out.println("Ingrese el ID de la cuenta para realizar el retiro:");
        int idCuenta = Integer.parseInt(sc.nextLine());
        System.out.println("Ingrese el monto a retirar:");
        int monto = Integer.parseInt(sc.nextLine());

        if (monto <= 0) {
            System.out.println("El monto debe ser positivo.");
            return;
        }

        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn != null) {
            
                PreparedStatement stmtSaldo = conn.prepareStatement(
                        "SELECT saldo FROM CuentaBancaria WHERE idCuenta = ?");
                stmtSaldo.setInt(1, idCuenta);
                ResultSet rs = stmtSaldo.executeQuery();

                if (rs.next()) {
                    int saldoActual = rs.getInt("saldo");

                    if (saldoActual < monto) {
                        System.out.println("Fondos insuficientes para realizar el retiro.");
                        return;
                    }

                    
                    PreparedStatement stmtRetiro = conn.prepareStatement(
                            "UPDATE CuentaBancaria SET saldo = saldo - ? WHERE idCuenta = ?");
                    stmtRetiro.setInt(1, monto);
                    stmtRetiro.setInt(2, idCuenta);
                    stmtRetiro.executeUpdate();

                    
                    PreparedStatement operacionStmt = conn.prepareStatement(
                            "INSERT INTO Operacion (idCuentaOrigen, tipoOperacion, monto, fechaOperacion) " +
                            "VALUES (?, 'RETIRO', ?, current_date)");
                    operacionStmt.setInt(1, idCuenta);
                    operacionStmt.setInt(2, monto);
                    operacionStmt.executeUpdate();

                    System.out.println("Retiro realizado exitosamente.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al realizar el retiro: " + e.getMessage());
        }
    }

    @Override
    public void realizarTransferencia() {
        System.out.println("Ingrese el ID de la cuenta de origen:");
        int idCuentaOrigen = Integer.parseInt(sc.nextLine());
        System.out.println("Ingrese el ID de la cuenta de destino:");
        int idCuentaDestino = Integer.parseInt(sc.nextLine());
        System.out.println("Ingrese el monto a transferir:");
        int monto = Integer.parseInt(sc.nextLine());

        if (monto <= 0) {
            System.out.println("El monto debe ser positivo.");
            return;
        }

        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn != null) {
            
                allableStatement stmt = conn.prepareCall("{CALL transferirFondos(?, ?, ?)}");
                stmt.setInt(1, idCuentaOrigen);
                stmt.setInt(2, idCuentaDestino);
                stmt.setInt(3, monto);
            
                stmt.execute();

                System.out.println("Transferencia realizada exitosamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al realizar la transferencia: " + e.getMessage());
        }

    }

    @Override
    public void consultarSaldo(String email) {
        DatabaseConnection dbConnection = new DatabaseConnection();

        try {
            if (dbConnection.getConnection() != null) {
                Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("SELECT idCuenta, saldo FROM cuentabancaria WHERE email = '" + email + "'");

                System.out.println("Usted tiene las siguientes cuentas:");
                int saldoTotal = 0;
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("idCuenta"));
                    System.out.println("Saldo: $" + rs.getInt("saldo"));
                    saldoTotal += rs.getInt("saldo");
                    System.out.println();
                }

                System.out.println("El total de dinero es sus cuentas es:  $" + saldoTotal);

            }
        } catch (SQLException e) {
            System.err.println("Error al registrar cliente en la base de datos");
        }
    }

    @Override
    public void mostrarOpcionesAdministrador() {

    }

    @Override
    public void consultarHistorialTransacciones() {

    }

    @Override
    public void generarReporteFinanciero() {

    }

    @Override
    public void verCuentasInactivas() {

    }

    @Override
    public void configurarUsuarios() {

    }
}
