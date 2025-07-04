package com.smartshop.productos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "etiquetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String tipo; // Ej: "dinámica", "estática", "ia", etc.

    @ManyToMany(mappedBy = "etiquetas")
    private Set<Producto> productos;
}

