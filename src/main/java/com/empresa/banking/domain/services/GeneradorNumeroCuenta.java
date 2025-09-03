package com.empresa.banking.domain.services;

import com.empresa.banking.domain.entities.TipoCuenta;
import java.util.Random;

public class GeneradorNumeroCuenta {

    private static final Random random = new Random();

    public static String generar(TipoCuenta tipoCuenta) {
        String prefijo = tipoCuenta.getPrefijo();
        String sufijo = String.format("%08d", random.nextInt(100000000));
        return prefijo + sufijo;
    }

    public static boolean esNumeroValido(String numeroCuenta, TipoCuenta tipoCuenta) {
        if (numeroCuenta == null || numeroCuenta.length() != 10) {
            return false;
        }

        return numeroCuenta.startsWith(tipoCuenta.getPrefijo()) &&
                numeroCuenta.substring(2).matches("\\d{8}");
    }
}
