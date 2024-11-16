package org.service;

import org.db.DatabaseConnection;
import org.model.Cliente;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class sistemaIMPL implements sistema{

    //Singleton sistema
    private static sistemaIMPL instance;
    private sistemaIMPL() {}

    public static sistemaIMPL getInstance() {
        if (instance == null) {
            instance = new sistemaIMPL();
        }
        return instance;
    }

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
        
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

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
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
    
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

        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

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

        do {
            System.out.println("Bienvenido! Seleccione una opcion:");
            System.out.println("\n 1) Realizar una operacion \n 2) Abrir una cuenta nueva \n 3) Consultar Saldo");
            System.out.print(">> ");
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
                case 0:
                    System.out.println("Saliendo del sistema...");
                    return;
                default:
                    System.out.println("Ingrese una opcion valida...");
                    break;
            }
        } while (true);
    }

    @Override
    public void realizarOperacion(String email) {
        do {
            System.out.println("Seleccione el tipo de operación que desea realizar:");
            System.out.println("1) Depósito");
            System.out.println("2) Retiro");
            System.out.println("3) Transferencia");
            System.out.println("0) Salir");
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
                case 0:
                    System.out.println("");
                    return;
                default:
                    System.out.println("Opción inválida, intente de nuevo.");
                    realizarOperacion(email);
            }
        } while (true);
    }

    @Override
    public void realizarDeposito(String email) {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn != null) {
            
                PreparedStatement stmtCuentas = conn.prepareStatement(
                        "SELECT idCuenta, saldo FROM CuentaBancaria WHERE email = ?");
                stmtCuentas.setString(1, email);
                ResultSet rs = stmtCuentas.executeQuery();

                System.out.println("Cuentas disponibles para depósito:");
                while (rs.next()) {
                    System.out.println("ID de cuenta: " + rs.getInt("idCuenta") + " - Saldo: $" + rs.getInt("saldo"));
                }

                System.out.println("Ingrese el ID de la cuenta para realizar el depósito:");
                int idCuenta = Integer.parseInt(sc.nextLine());
                System.out.println("Ingrese el monto a depositar:");
                int monto = Integer.parseInt(sc.nextLine());

                if (monto <= 0) {
                    System.out.println("El monto debe ser positivo.");
                    return;
                }

            
                PreparedStatement stmtDeposito = conn.prepareStatement(
                        "UPDATE CuentaBancaria SET saldo = saldo + ? WHERE idCuenta = ?");
                stmtDeposito.setInt(1, monto);
                stmtDeposito.setInt(2, idCuenta);
                stmtDeposito.executeUpdate();

            
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
    public void realizarRetiro(String email) {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn != null) {
            
                PreparedStatement stmtCuentas = conn.prepareStatement(
                        "SELECT idCuenta, saldo FROM CuentaBancaria WHERE email = ?");
                stmtCuentas.setString(1, email);
                ResultSet rs = stmtCuentas.executeQuery();

                System.out.println("Cuentas disponibles para retiro:");
                while (rs.next()) {
                    System.out.println("ID de cuenta: " + rs.getInt("idCuenta") + " - Saldo: $" + rs.getInt("saldo"));
                }

                System.out.println("Ingrese el ID de la cuenta para realizar el retiro:");
                int idCuenta = Integer.parseInt(sc.nextLine());
                System.out.println("Ingrese el monto a retirar:");
                int monto = Integer.parseInt(sc.nextLine());

                if (monto <= 0) {
                    System.out.println("El monto debe ser positivo.");
                    return;
                }
            
                PreparedStatement stmtSaldo = conn.prepareStatement(
                        "SELECT saldo FROM CuentaBancaria WHERE idCuenta = ?");
                stmtSaldo.setInt(1, idCuenta);
                ResultSet rsSaldo = stmtSaldo.executeQuery();

                if (rsSaldo.next()) {
                    int saldoActual = rsSaldo.getInt("saldo");

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
    public void realizarTransferencia(String email) {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn != null) {

                PreparedStatement stmtCuentasOrigen = conn.prepareStatement(
                        "SELECT idCuenta, saldo FROM CuentaBancaria WHERE email = ?");
                stmtCuentasOrigen.setString(1, email);
                ResultSet rsOrigen = stmtCuentasOrigen.executeQuery();

                System.out.println("Seleccione la cuenta de origen para la transferencia:");
                while (rsOrigen.next()) {
                    System.out.println("ID de cuenta: " + rsOrigen.getInt("idCuenta") + " - Saldo: $" + rsOrigen.getInt("saldo"));
                }

                System.out.print("Ingrese el ID de la cuenta de origen: ");
                int idCuentaOrigen = Integer.parseInt(sc.nextLine());

                System.out.print("Ingrese el correo del destinatario: ");
                String emailDestino = sc.nextLine();

                PreparedStatement stmtCuentasDestino = conn.prepareStatement(
                        "SELECT idCuenta, saldo FROM CuentaBancaria WHERE email = ?");
                stmtCuentasDestino.setString(1, emailDestino);
                ResultSet rsDestino = stmtCuentasDestino.executeQuery();

                System.out.println("Cuentas del destinatario:");
                while (rsDestino.next()) {
                    System.out.println("ID de cuenta: " + rsDestino.getInt("idCuenta") + " - Saldo: $" + rsDestino.getInt("saldo"));
                }

                System.out.print("Ingrese el ID de la cuenta de destino: ");
                int idCuentaDestino = Integer.parseInt(sc.nextLine());
                System.out.print("Ingrese el monto a transferir: ");
                int monto = Integer.parseInt(sc.nextLine());

                if (monto <= 0) {
                    System.out.println("El monto debe ser positivo.");
                    return;
                }

                CallableStatement stmtTransferencia = conn.prepareCall("CALL transferirFondos(?, ?, ?)");
                stmtTransferencia.setInt(1, idCuentaOrigen);
                stmtTransferencia.setInt(2, idCuentaDestino);
                stmtTransferencia.setInt(3, monto);
                stmtTransferencia.execute();

                System.out.println("Transferencia realizada exitosamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al realizar la transferencia: " + e.getMessage());
        }
    }

    @Override
    public void consultarSaldo(String email) {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

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
        do {
            System.out.println("Opciones del Administrador:");
            System.out.println("1) Consultar Historial de Transacciones");
            System.out.println("2) Generar Reporte Financiero");
            System.out.println("3) Vista de Cuentas Inactivas");
            System.out.println("4) Configuración de Usuarios");
            System.out.println("0) Salir");
            System.out.print(">> ");

            int opcion = Integer.parseInt(sc.nextLine());

            switch (opcion) {
                case 1:
                    consultarHistorialTransacciones();
                    break;
                case 2:
                    generarReporteFinanciero();
                    break;
                case 3:
                    verCuentasInactivas();
                    break;
                case 4:
                    configurarUsuarios();
                    break;
                case 0:
                    System.out.println("Saliendo del sistema...");
                    return;
                default:
                    System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                    break;
            }
        } while (true);
    }

    @Override
    public void consultarHistorialTransacciones() {
        System.out.println("Seleccione una opción para ver el historial de transacciones:");
        System.out.println("1) Ver todo el historial de transacciones");
        System.out.println("2) Filtrar por un período de tiempo específico");
        System.out.print(">> ");

        int opcion = Integer.parseInt(sc.nextLine());
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn != null) {
                String query;
                PreparedStatement stmt;

                if (opcion == 1) {
                    
                    query = "SELECT * FROM Operacion ORDER BY fecha_operacion DESC";
                    stmt = conn.prepareStatement(query);
                } else if (opcion == 2) {
                    
                    System.out.println("Ingrese la fecha de inicio (YYYY-MM-DD):");
                    String fechaInicio = sc.nextLine();
                    System.out.println("Ingrese la fecha de fin (YYYY-MM-DD):");
                    String fechaFin = sc.nextLine();

                    query = "SELECT * FROM Operacion WHERE fecha_operacion BETWEEN ? AND ? ORDER BY fecha_operacion DESC";
                    stmt = conn.prepareStatement(query);
                    stmt.setDate(1, java.sql.Date.valueOf(fechaInicio));
                    stmt.setDate(2, java.sql.Date.valueOf(fechaFin));
                } else {
                    System.out.println("Opción inválida.");
                    return;
                }
                
                ResultSet rs = stmt.executeQuery();
                System.out.println("Historial de Transacciones:");
                while (rs.next()) {
                    System.out.println("ID Transacción: " + rs.getInt("id_transaccion"));
                    System.out.println("Tipo de Operación: " + rs.getString("tipo_operacion"));
                    System.out.println("Monto: $" + rs.getInt("monto"));
                    System.out.println("Fecha de Operación: " + rs.getDate("fecha_operacion"));
                    System.out.println("Cuenta Origen: " + rs.getInt("id_cuenta_origen"));
                    System.out.println("Cuenta Destino: " + rs.getInt("id_cuenta_destino"));
                    System.out.println("---------------------------------");
                }

            }
        } catch (SQLException e) {
            System.err.println("Error al consultar el historial de transacciones: " + e.getMessage());
        }
    }

    @Override
    public void generarReporteFinanciero() {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn != null) {
               
                String saldoPromedioQuery = "SELECT AVG(saldo) AS saldo_promedio FROM cuentabancaria";
                PreparedStatement saldoPromStmt = conn.prepareStatement(saldoPromedioQuery);
                ResultSet rsSaldoProm = saldoPromStmt.executeQuery();
                if (rsSaldoProm.next()) {
                    System.out.println("Saldo promedio de las cuentas: $" + rsSaldoProm.getDouble("saldo_promedio"));
                }
                
                String cuentasTransaccionesQuery = 
                    "SELECT id_cuenta_origen AS id_cuenta, COUNT(*) AS transacciones " +
                    "FROM Operacion " +
                    "GROUP BY id_cuenta_origen " +
                    "ORDER BY transacciones DESC " +
                    "LIMIT 5";
                PreparedStatement cuentasTransStmt = conn.prepareStatement(cuentasTransaccionesQuery);
                ResultSet rsCuentasTrans = cuentasTransStmt.executeQuery();
                System.out.println("\nCuentas con mayor número de transacciones:");
                while (rsCuentasTrans.next()) {
                    System.out.println("ID Cuenta: " + rsCuentasTrans.getInt("id_cuenta"));
                    System.out.println("Número de Transacciones: " + rsCuentasTrans.getInt("transacciones"));
                    System.out.println("---------------------------------");
                }
                
                String ingresosNetosQuery = 
                    "SELECT " +
                    "(SELECT COALESCE(SUM(monto), 0) FROM Operacion WHERE tipo_operacion = 'DEPOSITO') - " +
                    "(SELECT COALESCE(SUM(monto), 0) FROM Operacion WHERE tipo_operacion = 'RETIRO') AS ingresos_netos";
                PreparedStatement ingresosNetosStmt = conn.prepareStatement(ingresosNetosQuery);
                ResultSet rsIngresosNetos = ingresosNetosStmt.executeQuery();
                if (rsIngresosNetos.next()) {
                    System.out.println("\nIngresos netos de la institución: $" + rsIngresosNetos.getDouble("ingresos_netos"));
                }

            }
        } catch (SQLException e) {
            System.err.println("Error al generar el reporte financiero: " + e.getMessage());
        }
    }

    @Override
    public void verCuentasInactivas() {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        System.out.println("Ingrese los cantidad de dias de inactividad por los que desea filtrar las cuentas inactivas:");
        System.out.print(">> ");
        int diasInactividad = Integer.parseInt(sc.nextLine());
        try {
            if (dbConnection.getConnection() != null) {
                Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("SELECT CB.idCuenta, CB.email, CB.saldo, CB.fechaCreacion\n" +
                        "FROM CuentaBancaria CB\n" +
                        "WHERE NOT EXISTS (\n" +
                        "    SELECT 1\n" +
                        "    FROM Operacion O\n" +
                        "    WHERE (O.idCuentaOrigen = CB.idCuenta OR O.idCuentaDestino = CB.idCuenta)\n" +
                        "      AND O.fechaOperacion BETWEEN CURRENT_DATE - INTERVAL '" + diasInactividad + " days' AND CURRENT_DATE\n" +
                        "); ");

                System.out.println("Las cuentas inactivas en un plazo de " + diasInactividad + " dias son: ");
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("idCuenta"));
                    System.out.println("Saldo: $" + rs.getInt("saldo"));
                    System.out.println("Mail: $" + rs.getString("email"));
                    System.out.println();
                }

            }
        } catch (SQLException e) {
            System.err.println("Error al registrar cliente en la base de datos");
        }
    }

    @Override
    public void configurarUsuarios() {

        do {
            System.out.println("Opciones del Administrador:");
            System.out.println("1) Agregar nuevo cliente");
            System.out.println("2) Modificar un cliente existente");
            System.out.println("3) Eliminar un cliente");
            System.out.println("0) Salir");
            System.out.print(">> ");

            int opcion = Integer.parseInt(sc.nextLine());

            String email = "";
            if (opcion != 0) {
                System.out.println("Indique el email de la persona a gestionar: ");
                System.out.println(">> ");
                email = sc.nextLine();
            }

            switch (opcion) {
                case 1:
                    registrarCliente(email);
                    break;
                case 2:
                    modificarClienteExistente(email);
                    break;
                case 3:
                    eliminarCliente(email);
                    break;
                case 0:
                    System.out.println("Saliendo del sistema...");
                    return;
                default:
                    System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                    break;
            }
        } while (true);

    }

private void eliminarCliente(String email) {
    DatabaseConnection dbConnection = DatabaseConnection.getInstance();

    try {
        if (dbConnection.getConnection() != null) {

            PreparedStatement deleteOperacionesStmt = dbConnection.getConnection().prepareStatement(
                    "DELETE FROM Operacion WHERE id_cuenta_origen IN (SELECT idCuenta FROM CuentaBancaria WHERE email = ?) " +
                    "OR id_cuenta_destino IN (SELECT idCuenta FROM CuentaBancaria WHERE email = ?)");
            deleteOperacionesStmt.setString(1, email);
            deleteOperacionesStmt.setString(2, email);
            deleteOperacionesStmt.executeUpdate();

            PreparedStatement deleteCuentasStmt = dbConnection.getConnection().prepareStatement(
                    "DELETE FROM CuentaBancaria WHERE email = ?");
            deleteCuentasStmt.setString(1, email);
            deleteCuentasStmt.executeUpdate();

            PreparedStatement deleteClienteStmt = dbConnection.getConnection().prepareStatement(
                    "DELETE FROM Cliente WHERE email = ?");
            deleteClienteStmt.setString(1, email);
            int tuplasAfectadas = deleteClienteStmt.executeUpdate();

            if (tuplasAfectadas > 0) {
                System.out.println("El cliente y sus datos asociados se han eliminado correctamente.");
            } else {
                System.out.println("No se encontró ningún cliente con el email proporcionado.");
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al eliminar al cliente: " + e.getMessage());
    }
}

    private void modificarClienteExistente(String email) {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        System.out.println("Ingrese los nuevos datos del cliente (deje vacío para no modificar):");
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Apellido: ");
        String apellido = sc.nextLine();
        System.out.print("Contraseña: ");
        String contrasenia = sc.nextLine();

        try {
            if (dbConnection.getConnection() != null) {
                
                PreparedStatement selectStmt = dbConnection.getConnection().prepareStatement(
                        "SELECT nombre, apellido, contrasenia FROM Cliente WHERE email = ?");
                selectStmt.setString(1, email);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    
                    String nuevoNombre = nombre.isEmpty() ? rs.getString("nombre") : nombre;
                    String nuevoApellido = apellido.isEmpty() ? rs.getString("apellido") : apellido;
                    String nuevaContrasenia = contrasenia.isEmpty() ? rs.getString("contrasenia") : contrasenia;
                    
                    PreparedStatement updateStmt = dbConnection.getConnection().prepareStatement(
                            "UPDATE Cliente SET nombre = ?, apellido = ?, contrasenia = ? WHERE email = ?");
                    updateStmt.setString(1, nuevoNombre);
                    updateStmt.setString(2, nuevoApellido);
                    updateStmt.setString(3, nuevaContrasenia);
                    updateStmt.setString(4, email);

                    int tuplasAfectadas = updateStmt.executeUpdate();

                    if (tuplasAfectadas > 0) {
                        System.out.println("Los datos del cliente se han modificado correctamente.");
                    } else {
                        System.out.println("No se encontró ningún cliente con el email proporcionado.");
                    }
                } else {
                    System.out.println("No se encontró ningún cliente con el email proporcionado.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al modificar los datos del cliente: " + e.getMessage());
        }
    }

}
