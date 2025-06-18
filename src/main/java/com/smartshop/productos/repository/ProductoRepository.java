package com.smartshop.productos.repository;

import com.smartshop.productos.entity.Producto;
import com.smartshop.productos.projection.CategoriaProductoCount;
import com.smartshop.productos.repository.custom.ProductoRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

//Interfaz que permite manipular los datos de la tabla Productos en la base
// de datos mediante mentodos CRUD, sin usar consultas SQL
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, ProductoRepositoryCustom {

    //=================================================================================================================================
    // Consulta personalizada para contar productos disponibles agrupados por categoría
    @Query("""
           SELECT p.categoria.id AS categoriaId, COUNT(p.id) AS total 
           FROM Producto p 
           WHERE p.disponible = true 
           GROUP BY p.categoria.id 
           HAVING COUNT(p.id) >= 10
           """)
    List<CategoriaProductoCount> findCategoriasConMinimo10ProductosDisponibles();

    //=================================================================================================================================

    // Consulta derivada por nombre: obtiene los 10 productos más recientes por categoría
    List<Producto> findTop10ByCategoriaIdAndDisponibleTrueOrderByIdDesc(Long categoriaId);

    //=================================================================================================================================

    @Query(
            value = """
        -- Selecciona todas las columnas del resultado final filtrado
        SELECT *
        FROM (
        -- Subconsulta: selecciona todos los productos disponibles en 3 categorías
            SELECT *,
                   -- Asigna un número de fila a cada producto dentro de su categoría, ordenado por ID descendente (los más recientes primero)
                   ROW_NUMBER() OVER (PARTITION BY categoria_id ORDER BY id DESC) AS fila
            FROM producto
            WHERE categoria_id IN (:cat1, :cat2, :cat3) -- ← Reemplaza con los 3 IDs reales de las categorías seleccionadas aleatoriamente
              AND disponible = true  -- Filtra solo los productos que están marcados como disponibles
        ) sub  -- Se le da el alias "sub" a la subconsulta para poder referenciarla afuera
        WHERE fila <= 15 -- Filtra para quedarnos solo con los primeros 15 productos de cada categoría (fila 1 a 15)
        """,
            nativeQuery = true
    )

    //Consulta los 15 primero productos por categoria aleatoria.
    List<Producto> findTop15ByCategorias(@Param("cat1") Long cat1,
                                         @Param("cat2") Long cat2,
                                         @Param("cat3") Long cat3);

    //=================================================================================================================================
    // Consulta derivada por nombre: encontrar productos por categoria
    Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCaseAndIdNotAndCategoriaIdAndPrecioBetweenAndDisponibleTrue(String nombreBase, Long productoId, Long categoriaId, BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);


}
