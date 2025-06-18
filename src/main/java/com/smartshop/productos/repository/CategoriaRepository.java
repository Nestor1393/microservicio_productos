package com.smartshop.productos.repository;

import com.smartshop.productos.entity.Categoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//Interfaz que permite manipular los datos de la tabla Categorias en la base
// de datos mediante mentodos CRUD, sin usar consultas SQL
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @EntityGraph(attributePaths = "subcategorias")
    List<Categoria> findByCategoriaPadreIsNull();

}

