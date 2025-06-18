package com.smartshop.productos.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaCreateDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        String descripcion
) {}

