package com.smartshop.productos.exception;

/**
 * Excepción lanzada cuando no hay suficientes categorías con productos disponibles para generar carruseles.
 */
public class NoHayCategoriasSuficientesException extends RuntimeException {

    public NoHayCategoriasSuficientesException(String mensaje) {
        super(mensaje);
    }

}
