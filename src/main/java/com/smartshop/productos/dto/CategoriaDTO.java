package com.smartshop.productos.dto;

import lombok.*;

import java.util.List;

//@Getter
//@Setter
//@AllArgsConstructor
//@Builder
public record CategoriaDTO(
        Long id,
        String nombre,
        String descripcion,
        List<CategoriaDTO> subcategorias
) {
}
