package com.smartshop.productos.service;

import com.smartshop.productos.dto.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductoService {

    // CRUD BÁSICO
    ProductoDTO crearProducto(ProductoCreateDTO productoCreateDTO);
    //================================================================================================================
    ProductoDTO obtenerProductoPorId(Long id);
    //================================================================================================================

    List<ProductoDTO> listarProductos();
    //================================================================================================================

    ProductoDTO actualizarProducto(Long id, ProductoUpdateDTO productoUpdateDTO);
    //================================================================================================================

    void eliminarProducto(Long id);
    //================================================================================================================

    // 1. Búsqueda avanzada y filtrado dinámico
    List<ProductoDTO> buscarProductos(String nombre, Long categoriaId, BigDecimal precioMin, BigDecimal precioMax, Boolean disponibles);
    //================================================================================================================

    // 2. Paginación y ordenamiento
    Page<ProductoDTO> listarProductosPaginado(int pagina, int tamanio, String ordenarPor, String direccion);
    //================================================================================================================

    // 3. Gestión de stock
    void ajustarStock(Long productoId, int cantidad);
    //================================================================================================================

    int obtenerStockDisponible(Long productoId);
    //================================================================================================================

    // 5. Recomendaciones inteligentes (básicas)
    Page<ProductoDTO> recomendarProductosSimilares(Long productoId, int pagina, int tamanio);
    //================================================================================================================

    // 8. Etiquetado inteligente / dinámico
    List<String> generarEtiquetasAutomaticas(Long productoId);
    //================================================================================================================

    // 9. Validación de disponibilidad
    boolean estaDisponible(Long productoId, int cantidadDeseada);
    //================================================================================================================

    // Método público expuesto por el servicio para obtener los carruseles
    List<CarruselDTO> obtenerCarruselesDeProductos();
    //================================================================================================================

    //Método para buscar productos páginados, tomando en cuenta, la página seleccionada y los filtros
    Page<ProductoDTO> buscarProductosPaginado(String nombre, Long categoriaId, BigDecimal precioMin, BigDecimal precioMax, Boolean disponibles, int pagina, int tamanio, String ordenarPor, String direccion);

    //================================================================================================================
    Page<ProductoDTO> obtenerProductosPorCategoriaPaginado(Long categoriaId, int pagina, int tamanio);

    //================================================================================================================

    ProductoDTO consultarProductoYRecomendar(Long productoId, Long usuarioId);

    Page<ProductoDTO> consultarRecomendacionesUltimoProductoUsuarioVisto(Long usuarioId, int pagina, int tamanio);



}

