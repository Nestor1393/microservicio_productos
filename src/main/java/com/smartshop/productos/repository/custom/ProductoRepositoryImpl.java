package com.smartshop.productos.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smartshop.productos.dto.ProductoDTO;
import com.smartshop.productos.entity.Producto;
import com.smartshop.productos.entity.QProducto;
import com.smartshop.productos.mapper.ProductoMapper;
import com.smartshop.productos.repository.util.QuerydslUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository // Marca esta clase como un componente de repositorio de Spring para ser detectado automáticamente
@RequiredArgsConstructor // Genera automáticamente un constructor con los atributos marcados como final
public class ProductoRepositoryImpl implements ProductoRepositoryCustom {

    private final EntityManager entityManager; // Se utiliza para construir la consulta QueryDSL
    private final ProductoMapper productoMapper; // Mapper que convierte entidades Producto a DTOs

    //=====================================================================================================================================================================
    @Override
    public List<ProductoDTO> buscarProductos(String nombre, Long categoriaId, BigDecimal precioMin, BigDecimal precioMax, Boolean disponibles) {

        // Creamos la fábrica de consultas QueryDSL usando el EntityManager
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // Instanciamos el objeto QProducto (clase generada por QueryDSL a partir de la entidad Producto)
        QProducto producto = QProducto.producto;

        // Instancia de BooleanBuilder para construir condiciones dinámicas
        BooleanBuilder builder = new BooleanBuilder();

        // Si se especifica un nombre para buscar...
        if (nombre != null && !nombre.isBlank()) {
            // Agrega condición: el nombre del producto debe contener el texto ignorando mayúsculas/minúsculas
            builder.and(producto.nombre.containsIgnoreCase(nombre));
        }

        // Si se especifica un ID de categoría...
        if (categoriaId != null) {
            // Agrega condición: el ID de la categoría del producto debe coincidir exactamente
            builder.and(producto.categoria.id.eq(categoriaId));
        }

        // Si se especifica un precio mínimo...
        if (precioMin != null) {
            // Agrega condición: el precio del producto debe ser mayor o igual al mínimo
            builder.and(producto.precio.goe(precioMin));
        }

        // Si se especifica un precio máximo...
        if (precioMax != null) {
            // Agrega condición: el precio del producto debe ser menor o igual al máximo
            builder.and(producto.precio.loe(precioMax));
        }

        // Si se especifica si debe estar disponible...
        if (disponibles != null) {
            // Agrega condición: el campo booleano disponible debe coincidir con el valor indicado
            builder.and(producto.disponible.eq(disponibles));
        }

        // Ejecutar la consulta select * from producto where (todas las condiciones del builder)
        List<Producto> productos = queryFactory
                .selectFrom(producto)  // Selecciona desde la tabla Producto
                .where(builder)       // Aplica el filtro dinámico construido
                .fetch();             // Ejecuta y obtiene los resultados como lista

        // Convertir cada entidad Producto en su correspondiente ProductoDTO
        return productos.stream()
                .map(productoMapper::toDto) // Mapea de Producto → ProductoDTO
                .toList();                  // Devuelve como lista inmutable (Java 17+)
    }

    //=====================================================================================================================================================================

    @Override
    public Page<ProductoDTO> buscarProductosPaginado(String nombre, Long categoriaId, BigDecimal precioMin, BigDecimal precioMax, Boolean disponibles, Pageable pageable) {
        // Se crea un JPAQueryFactory que será utilizado para construir la consulta con QueryDSL
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // Instancia de la clase generada por QueryDSL para la entidad Producto
        QProducto producto = QProducto.producto;

        // Instancia de BooleanBuilder para agregar las condiciones de los filtros dinámicos
        BooleanBuilder builder = new BooleanBuilder();

        // Condición para el nombre del producto, si no es null ni vacío
        if (nombre != null && !nombre.isBlank()) {
            builder.and(producto.nombre.containsIgnoreCase(nombre));  // Filtra por nombre, ignorando mayúsculas/minúsculas
        }

        // Condición para el ID de categoría, si no es null
        if (categoriaId != null) {
            builder.and(producto.categoria.id.eq(categoriaId));  // Filtra por ID de categoría
        }

        // Condición para el precio mínimo, si no es null
        if (precioMin != null) {
            builder.and(producto.precio.goe(precioMin));  // Filtra por precio mínimo
        }

        // Condición para el precio máximo, si no es null
        if (precioMax != null) {
            builder.and(producto.precio.loe(precioMax));  // Filtra por precio máximo
        }

        // Condición para la disponibilidad, si no es null
        if (disponibles != null) {
            builder.and(producto.disponible.eq(disponibles));  // Filtra por disponibilidad
        }

        // Ejecuta la consulta con los filtros dinámicos aplicados, limitados por la paginación
        List<Producto> resultados = queryFactory
                .selectFrom(producto)  // Selecciona la entidad Producto
                .where(builder)  // Aplica los filtros construidos
                .offset(pageable.getOffset())  // Aplica el offset para la paginación
                .limit(pageable.getPageSize())  // Limita el número de resultados por página
                //.orderBy(getOrderSpecifier(producto, pageable.getSort()))  // Aplica el ordenamiento
                .orderBy(QuerydslUtil.getOrderSpecifiersForProducto(pageable.getSort()))
                .fetch();  // Ejecuta la consulta

        // Consulta para contar el total de productos que cumplen con los filtros sin paginación
        long total = queryFactory
                .selectFrom(producto)
                .where(builder)
                .fetchCount();  // Obtiene el total de productos que cumplen los filtros

        // Convierte la lista de entidades Producto a DTOs
        List<ProductoDTO> dtoList = resultados.stream()
                .map(productoMapper::toDto)  // Convierte cada Producto a ProductoDTO
                .toList();  // Devuelve la lista de DTOs como una lista inmutable

        // Devuelve una página con los DTOs, los detalles de la paginación y el total de elementos
        return new PageImpl<>(dtoList, pageable, total);
    }

    //=====================================================================================================================================================================



    //=====================================================================================================================================================================

}

