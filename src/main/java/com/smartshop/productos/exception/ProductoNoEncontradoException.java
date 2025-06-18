package com.smartshop.productos.exception;

//La clase hereda de RuntimeException. Esto significa que es una excepción no verificada
public class ProductoNoEncontradoException  extends RuntimeException{
    //Este constructor llama al constructor de la clase RuntimeException, pasando el mensaje de error a la clase base para que se guarde.
    public ProductoNoEncontradoException(String mensaje) {
        //Llama al constructor de la clase base RuntimeException y le pasa el mensaje de error que se proporcionó al crear la excepción.
        super(mensaje);
    }
}
