package org;

import org.service.sistemaIMPL;

public class Main {
    public static void main(String[] args) {

        sistemaIMPL sis = sistemaIMPL.getInstance();
        sis.iniciarSesion();

    }
}