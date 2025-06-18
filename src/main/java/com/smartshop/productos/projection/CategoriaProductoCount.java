package com.smartshop.productos.projection;

// Proyección para obtener el ID de la categoría y el total de productos disponibles en ella.
public interface CategoriaProductoCount {
    Long getCategoriaId(); // Retorna el ID de la categoría
    Long getTotal();       // Retorna el total de productos disponibles en esa categoría
}