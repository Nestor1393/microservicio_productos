package com.smartshop.productos.controller;

import com.smartshop.productos.dto.CarruselDTO;
import com.smartshop.productos.dto.ProductoDTO;
import com.smartshop.productos.dto.ProductoDetalleResponse;
import com.smartshop.productos.security.annotations.RequiresAuth;
import com.smartshop.productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST que expone un endpoint para obtener carruseles de productos agrupados por categoría.
 */
@RestController // Indica que esta clase es un controlador REST, que maneja solicitudes HTTP y devuelve respuestas JSON.
@RequestMapping("/api/v1/productos") // Define la ruta base para todos los endpoints de esta clase.
@RequiredArgsConstructor
@Validated
// Genera automáticamente un constructor con los atributos marcados como final (inyección por constructor).
@Slf4j // Habilita el uso de un logger (log) para registrar información, advertencias y errores.
public class ProductoController {

    // Inyección del servicio ProductoService que contiene la lógica de negocio.
    //private final ProductoService productoService;
    private final ProductoService productoService;


    //===============================================================================================================================================
    /**
     * Endpoint que permite obtener carruseles de productos por categoría.
     */
    @GetMapping("/carruseles") // Define que este método se ejecuta cuando se hace una petición GET a /api/productos/carruseles
    public ResponseEntity<List<CarruselDTO>> obtenerCarruselesDeProductos() {
        // Se registra en los logs que el controlador ha recibido la solicitud para obtener carruseles
        log.info("Solicitud recibida para obtener carruseles de productos.");

        // Se delega la obtención de carruseles al servicio, que contiene la lógica de negocio.
        List<CarruselDTO> carruseles = productoService.obtenerCarruselesDeProductos();

        // Se devuelve una respuesta HTTP 200 OK con el mapa de categorías y productos.
        return ResponseEntity.ok(carruseles);
    }
    //===============================================================================================================================================

    // Indicamos que este método responderá a solicitudes HTTP GET (por convención REST para listar recursos)
    @GetMapping("/pagina/seleccionada")
    public ResponseEntity<Page<ProductoDTO>> listarProductosPaginado(

            // Parámetro 'pagina', valor por defecto 0. Debe ser ≥ 0
            @RequestParam(defaultValue = "0")
            @Min(0) int pagina,

            // Parámetro 'tamanio', valor por defecto 10. Debe estar entre 1 y 100
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(100) int tamanio,

            // Parámetro 'ordenarPor', campo por el cual ordenar los productos. Por defecto 'nombre'
            @RequestParam(defaultValue = "nombre")
            String ordenarPor,

            // Parámetro 'direccion', que define si el orden es ascendente o descendente
            // Solo acepta 'asc' o 'desc', sin importar mayúsculas/minúsculas (CASE_INSENSITIVE)
            @RequestParam(defaultValue = "asc")
            //@Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE,
                    //message = "La dirección debe ser 'asc' o 'desc'")
            String direccion) {

        // Registramos en el log los parámetros recibidos para trazabilidad
        log.info("GET /api/v1/productos/pagina/seleccionada - página={}, tamaño={}, ordenarPor={}, direccion={}",
                pagina, tamanio, ordenarPor, direccion);

        // Llamamos al servicio para obtener la página de productos con los criterios indicados
        Page<ProductoDTO> productos = productoService.listarProductosPaginado(
                pagina, tamanio, ordenarPor, direccion);

        // Devolvemos la página de productos con estado HTTP 200 OK
        return ResponseEntity.ok(productos);
    }
    //===============================================================================================================================================

    @GetMapping("/filtrar")
    //@Operation(summary = "Buscar productos con filtros y paginación", description = "Permite buscar productos por nombre, categoría, rango de precios y disponibilidad, con soporte de paginación y ordenamiento.")
    @Operation(
            summary = "Buscar productos con filtros y paginación",
            description = "Permite buscar productos por nombre, categoría, precios y disponibilidad. Soporta paginación y ordenamiento."
    )
    public ResponseEntity<Page<ProductoDTO>> buscarProductosPaginado(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Boolean disponibles,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int tamanio,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion
    ) {
        // Registramos en el log los parámetros recibidos para trazabilidad
        log.info("GET /api/v1/productos/filtrar - nombre={}, categoriaId={}, precioMin={}, precioMax={}, disponibles={}, página={}, tamaño={}, ordenarPor={}, direccion={}",
                nombre, categoriaId, precioMin, precioMax, disponibles, pagina, tamanio, ordenarPor, direccion);

        log.info("Petición para buscar productos con filtros y paginación recibida");

        Page<ProductoDTO> resultado = productoService.buscarProductosPaginado(
                nombre, categoriaId, precioMin, precioMax, disponibles, pagina, tamanio, ordenarPor, direccion
        );

        return ResponseEntity.ok(resultado);
    }

    //===============================================================================================================================================

    @GetMapping("/categoria/{categoriaId}")
    @Operation(
            summary = "Buscar productos por categoria",
            description = "Permite buscar productos por categoria con paginación"
    )
    public ResponseEntity<Page<ProductoDTO>> obtenerProductosPorCategoria(
            @PathVariable Long categoriaId,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int tamanio
    ){
        log.info("GET /api/v1/productos/categoria/{} categoriaId={}, pagina={}, tamanio={}",categoriaId, pagina, tamanio );

        return  ResponseEntity.ok(productoService.obtenerProductosPorCategoriaPaginado(categoriaId, pagina, tamanio));
    }


    @RequiresAuth
    @GetMapping("/{idProducto}/detalle")
    @SecurityRequirement(name = "BearerAuth") // Indica que este endpoint requiere el esquema BearerAuth
    @Operation(
            summary = "Consultar detalle del producto y buscar los productos similares",
            description = "Permite consultar los detalles del producto, registrar el historial y consultar  productos similares"
    )
    public ResponseEntity<ProductoDetalleResponse> verDetalleProducto(
            @PathVariable Long idProducto,
            //@RequestParam Long usuarioId, // en app real, vendría del token
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int tamanio,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Este será el 'subject' que pusiste al generar el token
        Long usuarioId = Long.parseLong(userDetails.getUsername());

        log.info("GET /api/v1/productos/{idProducto}/detalle idProducto={}, usuarioId={}, pagina={}, tamanio={}",idProducto, usuarioId, pagina, tamanio );

        ProductoDTO producto = productoService.consultarProductoYRecomendar(idProducto, usuarioId);
        Page<ProductoDTO> recomendaciones = productoService.recomendarProductosSimilares(idProducto, pagina, tamanio);

        return ResponseEntity.ok(new ProductoDetalleResponse(producto, recomendaciones));
    }

    @GetMapping("/recomendaciones")
    @Operation(
            summary = "Consulta productos de la misma categoria del último visto",
            description = "Permite consultar los productos similares al ultimo registro de navegación"
    )

    public ResponseEntity<Page<ProductoDTO>> consultarRecomendacionesUltimoProductoUsuarioVisto(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int tamanio
    ){

        log.info("GET /api/v1/productos/recomendaciones usuarioId={}, pagina={}, tamanio={}",usuarioId, pagina, tamanio);

        Page<ProductoDTO> productosRecomendados = productoService.consultarRecomendacionesUltimoProductoUsuarioVisto(usuarioId, pagina, tamanio);

        return ResponseEntity.ok(productosRecomendados);
    }


    @PostMapping("/{id}/etiquetas/ia")
    public ResponseEntity<List<String>> generarEtiquetasIA(@PathVariable Long id) {
        List<String> etiquetas = productoService.generarEtiquetasAutomaticas(id);
        System.out.println("Etiquetas fallido");
        return ResponseEntity.ok(etiquetas);
    }

}
