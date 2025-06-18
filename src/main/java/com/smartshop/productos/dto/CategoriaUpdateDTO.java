package com.smartshop.productos.dto;

import jakarta.validation.constraints.*;
import lombok.*;

//@Getter
//@Setter
//@AllArgsConstructor
//@Builder
public record CategoriaUpdateDTO(

        @NotNull(message = "El ID de categoria es obligatorio")
        Long id,

        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres")
        String descripcion
) {
}
