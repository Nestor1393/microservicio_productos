package com.smartshop.productos.repository.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.smartshop.productos.entity.QProducto;
import org.springframework.data.domain.Sort;

public class QuerydslUtil {

    // Método que convierte el orden de Spring (Sort) a orden QueryDSL (OrderSpecifier)
    public static OrderSpecifier<?>[] getOrderSpecifiersForProducto(Sort sort) {

        // Si no se especificó ordenamiento o está vacío, devuelve un array vacío (sin ordenar)
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier[0];
        }

        // Crea una instancia de QProducto, que representa la entidad Producto en QueryDSL
        QProducto producto = QProducto.producto;

        // Recorre cada campo por el cual se quiere ordenar (pueden ser varios), los convierte a OrderSpecifier
        return sort.stream()
                .map(order -> {
                    String property = order.getProperty(); // Obtiene el nombre del campo (ej: "nombre", "precio")

                    // Determina si el orden es ascendente o descendente
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                    // Usa switch para crear el OrderSpecifier solo si el campo está permitido
                    return switch (property) {
                        case "nombre" -> new OrderSpecifier<>(direction, producto.nombre); // Ordena por nombre
                        case "precio" -> new OrderSpecifier<>(direction, producto.precio); // Ordena por precio
                        default -> throw new IllegalArgumentException("Campo de ordenamiento no válido: " + property);
                        // Si se intenta ordenar por un campo no permitido, lanza una excepción
                    };
                })
                // Convierte el Stream de OrderSpecifiers a un arreglo
                .toArray(OrderSpecifier[]::new);
    }
}

