package com.smartshop.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
public class ProductoDetalleResponse {
    private ProductoDTO producto;
    private Page<ProductoDTO> recomendaciones;
}