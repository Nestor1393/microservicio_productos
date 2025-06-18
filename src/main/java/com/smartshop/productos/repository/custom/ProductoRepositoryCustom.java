package com.smartshop.productos.repository.custom;

import com.smartshop.productos.dto.ProductoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoRepositoryCustom {


    //=======================================================================================================================================
    /**
     * Consulta dinámica de productos filtrando por varios parámetros.
     */
    List<ProductoDTO> buscarProductos(String nombre, Long categoriaId, BigDecimal precioMin, BigDecimal precioMax, Boolean disponibles);
    //=======================================================================================================================================

    /**
     Método que permite fconsultar productos por filtro y paginado
     */
    Page<ProductoDTO> buscarProductosPaginado(
            String nombre,                // Parámetro de filtro para el nombre del producto
            Long categoriaId,             // Parámetro de filtro para la categoría del producto
            BigDecimal precioMin,         // Filtro para precio mínimo
            BigDecimal precioMax,         // Filtro para precio máximo
            Boolean disponibles,          // Filtro para la disponibilidad del producto
            Pageable pageable
    );
}
