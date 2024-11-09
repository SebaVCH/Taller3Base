package org.model;

import java.time.LocalDate;

public class cuentaBancaria {

    int idCuenta,saldo;
    String email;
    LocalDate fechacreacion;

    public cuentaBancaria(String email) {
        this.email = email;
        this.saldo = 1000;
    }
}
