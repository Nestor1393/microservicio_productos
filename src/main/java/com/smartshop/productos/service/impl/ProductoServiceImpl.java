package com.smartshop.productos.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartshop.productos.dto.*;
import com.smartshop.productos.entity.Etiqueta;
import com.smartshop.productos.entity.HistorialNavegacion;
import com.smartshop.productos.entity.Producto;
import com.smartshop.productos.exception.NoHayCategoriasSuficientesException;
import com.smartshop.productos.exception.ProductoNoEncontradoException;
import com.smartshop.productos.mapper.CategoriaMapper;
import com.smartshop.productos.mapper.ProductoMapper;
import com.smartshop.productos.repository.CategoriaRepository;
import com.smartshop.productos.repository.EtiquetaRepository;
import com.smartshop.productos.repository.HistorialNavegacionRepository;
import com.smartshop.productos.repository.ProductoRepository;
import com.smartshop.productos.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

//Anotación de Spring que indica que esta clase es un servicio (lógica de negocio).
@Service
//De Lombok. Genera automáticamente un constructor con los campos final (Inyección de dependecias).
@RequiredArgsConstructor
// Lombok, permite registrar mensajes de log fácilmente.
@Slf4j
//Implementa la interfaz ProductoService, lo que obliga a definir los métodos que allí se declararon
public class ProductoServiceImpl implements ProductoService {
    //Campos inyectados automáticamente gracias a @RequiredArgsConstructor
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    private final HistorialNavegacionRepository historialNavegacionRepository;
    private final EtiquetaRepository etiquetaRepository;

    //===============================================================================================================================================
    //Indica que este método sobrescribe uno definido en la interfaz ProductoService.
    @Override
    //Anotación de Spring, Si ocurre una excepción, Spring hace rollback automáticamente.
    @Transactional
    //Recibe un DTO con los datos, retorna un ProductoDTO con los datos del producto ya creado.
    public ProductoDTO crearProducto(ProductoCreateDTO productoCreateDTO) {
        //Registra en los logs un mensaje informando que se está creando un nuevo producto.
        log.info("Creando nuevo producto: {}", productoCreateDTO.getNombre());

        //Convierte el DTO de entrada (ProductoCreateDTO) a una entidad Producto.
        Producto producto = productoMapper.toEntityFromCreateDTO(productoCreateDTO);

        producto.setVecesVisto(0);
        producto.setDisponible(producto.getStock() > 0);

        Producto productoGuardado = productoRepository.save(producto);

        log.info("Producto guardado con ID: {}", productoGuardado.getId());

        //Convierte la entidad Producto persistida a un ProductoDTO.
        return productoMapper.toDto(productoGuardado);
    }

    //===============================================================================================================================================

    @Override
    //Asegura que este método es solo para lectura.
    @Transactional(readOnly = true)
    public ProductoDTO obtenerProductoPorId(Long id) {

        log.info("Buscando producto con ID: {}", id);
        // Se busca el producto en la base de datos utilizando el repositorio. Si no se encuentra, se lanza la excepción ProductoNoEncontradoException.
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + id));

        // Se convierte la entidad Producto encontrada a un ProductoDTO para enviar solo los datos necesarios al cliente
        return productoMapper.toDto(producto);
    }

    //===============================================================================================================================================

    @Override
    @Transactional(readOnly = true)  // Este método solo realiza lecturas, no modifica datos.
    public List<ProductoDTO> listarProductos() {
        log.info("Listando todos los productos");

        // Se obtiene la lista de entidades Producto desde la base de datos
        List<Producto> productos = productoRepository.findAll();

        // Se convierte la lista de entidades a una lista de DTOs usando el mapper

        //Convierte la lista de productos (List<Producto>) en un Stream.
        //.stream() Un Stream es una secuencia de elementos que se pueden procesar de forma funcional
        // (con operaciones como map, filter, collect, etc.).
        //.map(productoMapper::toDto) Por cada objeto Producto en la lista, se ejecuta el método productoMapper.toDto(...),
        // que convierte la entidad a un DTO.
        //.toList() recoge todos los elementos del Stream transformado y los convierte
        // en una nueva lista inmutable (List<ProductoDTO>).
        // los "::" significan method reference operator  y permiten hacer referencia a un metodo, que sera
        //utilizdo como una función. productoMapper::toDto es igual a:  (producto) -> productoMapper.toDto(producto).
        return productos.stream()
                .map(productoMapper::toDto)
                .toList();  // Retorna una lista inmutable de ProductoDTO

    }

    //===============================================================================================================================================

    @Override
    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoUpdateDTO productoUpdateDTO) {
        log.info("Actualizando producto con ID: {}", id);

        // Buscar el producto por ID, o lanzar excepción si no existe, en este caso se está utilizando
        //un Optional implicito ya que findById() devuelve un Optional.
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado para actualizar con ID: " + id));

        // Actualizar los campos modificables desde el DTO
        productoExistente.setNombre(productoUpdateDTO.getNombre());
        productoExistente.setDescripcion(productoUpdateDTO.getDescripcion());
        productoExistente.setPrecio(productoUpdateDTO.getPrecio());
        productoExistente.setStock(productoUpdateDTO.getStock());
        productoExistente.getCategoria().setId(productoUpdateDTO.getCategoriaId());

        // Recalcular disponibilidad en base al nuevo stock
        productoExistente.setDisponible(productoExistente.getStock() > 0);

        // Guardar cambios
        Producto productoActualizado = productoRepository.save(productoExistente);

        log.info("Producto con ID {} actualizado correctamente.", id);

        return productoMapper.toDto(productoActualizado);
}

    //===============================================================================================================================================


    @Override
    //Este método debe ejecutarse como una transacción completa. Si algo falla, deshaz
    // todo (Atómico = todo o nada.)
    @Transactional
    public void eliminarProducto(Long id) {
        log.info("Eliminando producto con ID: {}", id); // Log de inicio para rastrear la operación

        // Buscar el producto por ID, o lanzar una excepción si no existe (uso de Optional implícito)
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado para eliminar con ID: " + id));

        // Eliminar el producto encontrado de la base de datos
        productoRepository.delete(producto);

        log.info("Producto con ID {} eliminado correctamente.", id); // Log de confirmación de eliminación

    }

    //===============================================================================================================================================

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarProductos(String nombre, Long categoriaId, BigDecimal precioMin, BigDecimal precioMax, Boolean disponibles) {
        log.info("Buscando productos con filtros - nombre: {}, categoriaId: {}, precioMin: {}, precioMax: {}, disponibles: {}",
                nombre, categoriaId, precioMin, precioMax, disponibles);

        // Llama al repositorio personalizado que utiliza QueryDSL
            return productoRepository.buscarProductos(nombre, categoriaId, precioMin, precioMax, disponibles);
    }

    //===============================================================================================================================================

    /*
    El método listarProductosPaginado permitirá obtener una lista de productos de la base de datos aplicando filtros dinámicos (nombre, categoría, precio, disponibilidad) y, además,
    ofrecerá soporte para paginación y ordenamiento. La paginación se realiza utilizando los parámetros pagina y tamanio, que permiten controlar la cantidad de resultados por página,
    mientras que los parámetros ordenarPor y direccion permiten ordenar los resultados por cualquier atributo de los productos (por ejemplo, por precio, nombre, etc.), y especificar si el orden es ascendente o descendente.
    Este método debe devolver una página de resultados (Page<ProductoDTO>) que incluye la información de los productos solicitados, el total de elementos, la página actual y otros detalles útiles para manejar la paginación en la interfaz de usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarProductosPaginado(int pagina, int tamanio, String ordenarPor, String direccion) {
        log.info("Buscando productos con paginación - página: {}, tamaño: {}, ordenarPor: {}, dirección: {}", pagina, tamanio, ordenarPor, direccion);

        //Con esto, el método evita romperse si el frontend o el usuario envía un campo incorrecto para ordenar. En lugar de fallar con una excepción 500, simplemente se devuelve la página sin ordenar.
        //Declara una variable sort de tipo Sort, que almacenará el criterio de ordenamiento a aplicar en la consulta (ascendente o descendente).
        Sort sort;
        try {
            //Sort.by(ordenarPor) genera el criterio de orden basado en el nombre del campo (por ejemplo "precio").
            sort = direccion.equalsIgnoreCase("asc") ? Sort.by(ordenarPor).ascending() : Sort.by(ordenarPor).descending();
        //ocurre si el campo proporcionado para ordenar no es válido o no existe en la entidad JPA.
        } catch (PropertyReferenceException e) {
            log.warn("Campo de ordenamiento no válido '{}', se ignorará el ordenamiento", ordenarPor);
            // En caso de excepción, se asigna Sort.unsorted(), que indica que no se aplicará ningún criterio de ordenamiento. Esto permite que la aplicación continúe sin fallar.
            sort = Sort.unsorted();
        }

        // Creamos un objeto Pageable que define cómo se deben paginar y ordenar los resultados
        //Sort.by(ordenarPor): Especifica el campo por el cual se deben ordenar los resultados. El parámetro ordenarPor es el nombre del campo (como nombre o precio).

        //Pageable pageable = PageRequest.of(pagina, tamanio, direccion.equalsIgnoreCase("asc") ? Sort.by(ordenarPor).ascending() : Sort.by(ordenarPor).descending());
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);

        // Llamamos al repositorio para obtener una página de productos con los parámetros de paginación y ordenamiento
        Page<Producto> productos = productoRepository.findAll(pageable);

        // Convertimos cada entidad Producto en un ProductoDTO usando el mapper
        return productos.map(productoMapper::toDto);  // El método map() aplica la conversión de entidad a DTO en cada elemento de la página
    }

    //===============================================================================================================================================

    @Override
    public void ajustarStock(Long productoId, int cantidad) {

    }

    //===============================================================================================================================================

    @Override
    public int obtenerStockDisponible(Long productoId) {
        return 0;
    }

    //===============================================================================================================================================


    @Override
    public Page<ProductoDTO> recomendarProductosSimilares(Long productoId, int pagina, int tamanio) {

        log.info("Recomendando productos similares al ID: {}", productoId);

        Producto productoBase = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        String nombreBase = productoBase.getNombre();
        String keyword = nombreBase.split(" ")[0];

        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());

        BigDecimal min = productoBase.getPrecio().multiply(BigDecimal.valueOf(0.8));
        BigDecimal max = productoBase.getPrecio().multiply(BigDecimal.valueOf(1.2));

        Page<Producto> similares = productoRepository
                .findByNombreContainingIgnoreCaseAndIdNotAndCategoriaIdAndPrecioBetweenAndDisponibleTrue(keyword, productoId,productoBase.getCategoria().getId(), min, max, pageable);
        // Convertimos a DTOs
        return similares.map(productoMapper::toDto);
    }

    //===============================================================================================================================================

    @Override
    @Transactional
    public List<String> generarEtiquetasAutomaticas(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        String inputTexto = producto.getNombre() + ". " + producto.getDescripcion();

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-inference.huggingface.co/models/ml6team/keyphrase-extraction-kbir-openkp"))
                    //.header("Authorization", "Bearer ") // Reemplaza con tu token HuggingFace
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"inputs\":\"" + inputTexto + "\"}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Respuesta cruda de HuggingFace: {}", response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error al llamar HuggingFace: " + response.statusCode() + " - " + response.body());
            }

            ObjectMapper mapper = new ObjectMapper();
            List<List<Map<String, String>>> raw = mapper.readValue(response.body(), List.class);

            List<String> etiquetas = raw.get(0).stream()
                    .map(e -> e.get("word"))
                    .distinct()
                    .collect(Collectors.toList());

            // Guardar etiquetas si no existen y asociarlas al producto
            for (String nombreEtiqueta : etiquetas) {
                Etiqueta etiqueta = etiquetaRepository.findByNombre(nombreEtiqueta)
                        .orElseGet(() -> etiquetaRepository.save(Etiqueta.builder()
                                .nombre(nombreEtiqueta)
                                .tipo("ia")
                                .build()));

                producto.getEtiquetas().add(etiqueta);
            }

            productoRepository.save(producto);

            return etiquetas;
        } catch (Exception e) {
            log.error("Error al generar etiquetas con HuggingFace", e);
            return Collections.emptyList();
        }
    }


    //===============================================================================================================================================

    @Override
    public boolean estaDisponible(Long productoId, int cantidadDeseada) {
        return false;
    }

    //===============================================================================================================================================

    @Override
    @Transactional(readOnly = true) // Indica que esta operación es de solo lectura y no modificará datos en la base de datos.

    public List<CarruselDTO> obtenerCarruselesDeProductos() {
        //Registra un mensaje en los logs para indicar que el proceso ha comenzado.
        log.info("Obteniendo carruseles de productos por categoría...");

        //Obtiene los IDs de categorías que tienen al menos 10 productos disponibles (desde una consulta personalizada del repositorio).
        var categoriaIdsValidas = productoRepository.findCategoriasConMinimo10ProductosDisponibles()
                .stream() //Usa stream() para convertir el resultado en una lista de tipo List<Long>
                .map(c -> c.getCategoriaId()) //mapea los resultados para extraer solo los IDs (categoriaId) y los guarda en una lista.
                .toList();

        //Verifica que haya al menos 3 categorías con suficientes productos para armar los carruseles.
        if (categoriaIdsValidas.size() < 3) {
            log.warn("Se encontraron {} categorías con al menos 10 productos disponibles. Se requieren 3.", categoriaIdsValidas.size());
            //Si no se cumple, lanza una excepción y se detiene el proceso.
            throw new NoHayCategoriasSuficientesException("No hay suficientes categorías con productos disponibles.");
        }
        //Se convierte la lista inmutable a una lista mutable antes de hacer operaciones como shuffle.
        List<Long> categoriaIdsValidasMutable = new ArrayList<>(categoriaIdsValidas);
        //Desordena aleatoriamente la lista de categorías válidas para que los carruseles seleccionados no siempre sean los mismos (shuffle = aleatorio, barajear).
        Collections.shuffle(categoriaIdsValidasMutable);

        //Toma las primeras 3 categorías después del shuffle, es decir, selecciona 3 categorías al azar, (utilizamos stream.toList para volver inmutable de nuevo la lista).
        var categoriasSeleccionadas = categoriaIdsValidasMutable.stream().toList().subList(0, 3);

        //Se usa LinkedHashMap para mantener el orden de inserción (el orden de los carruseles seleccionados).
        //var resultado = new ArrayList<CarruselDTO>();
        List<CarruselDTO> resultado = new ArrayList<>();


        //var categorias = categoriaRepository.findAllById(categoriasSeleccionadas);
        //var categoriaMap = categorias.stream().collect(Collectors.toMap(Categoria::getId, Function.identity()));


        //Itera por cada una de las 3 categorías seleccionadas aleatoriamente.
        for (var categoriaId : categoriasSeleccionadas) {
            //Busca la categoría completa en la base de datos por su ID.
            var categoria = categoriaRepository.findById(categoriaId)
                    //Si no existe, lanza una excepción
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + categoriaId));

            //Consulta los 10 productos de tipo List<Producto> más recientes y disponibles para esa categoría (ordenados por ID descendente).
                 var productos = productoRepository
             //List<ProductoDTO> productos =  productoRepository
                    .findTop10ByCategoriaIdAndDisponibleTrueOrderByIdDesc(categoriaId)
                    .stream()
                    .parallel() // permite ejecutar operaciones de stream en múltiples hilos en paralelo, aprovechando los núcleos del procesador para mejorar el rendimiento.
                    .map(productoMapper::toDto)//los convierte a DTO usando el mapper
                    .toList();
            //Convierte la categoría a CategoriaDTO y la asocia con la lista de ProductoDTO dentro del mapa resultado.
            //resultado.put(categoriaMapper.toDTO(categoria), productos);

            resultado.add(new CarruselDTO(categoriaMapper.toDTO(categoria),productos));
        }

        log.info("Carruseles generados exitosamente.");
        //Devuelve la lista con 3 categorías y sus respectivos 10 productos disponibles.
        return resultado;
    }

    //===============================================================================================================================================

    @Override
    @Transactional(readOnly = true)  // Anotación para garantizar que el método no modifica el estado de la base de datos
    public Page<ProductoDTO> buscarProductosPaginado(
            String nombre,                // Parámetro de filtro para el nombre del producto
            Long categoriaId,             // Parámetro de filtro para la categoría del producto
            BigDecimal precioMin,         // Filtro para precio mínimo
            BigDecimal precioMax,         // Filtro para precio máximo
            Boolean disponibles,          // Filtro para la disponibilidad del producto
            int pagina,                   // Número de página para la paginación
            int tamanio,                  // Tamaño de la página para la paginación
            String ordenarPor,            // Atributo por el cual ordenar los resultados
            String direccion              // Dirección del orden (ascendente o descendente)
    ) {

        log.info("Buscando productos paginados con filtros - nombre: {}, categoriaId: {}, precioMin: {}, precioMax: {}, disponibles: {}, página: {}, tamaño: {}, ordenarPor: {}, dirección: {}",
                nombre, categoriaId, precioMin, precioMax, disponibles, pagina, tamanio, ordenarPor, direccion);

        // Determina la dirección del ordenamiento (ascendente o descendente)
        Sort sort;
        try {
            sort = direccion.equalsIgnoreCase("asc") ? Sort.by(ordenarPor).ascending() : Sort.by(ordenarPor).descending();
        } catch (PropertyReferenceException e) {
            // Si el campo de ordenamiento no es válido, se devuelve sin ordenamiento
            log.warn("Campo de ordenamiento no válido '{}', se ignorará el ordenamiento", ordenarPor);
            sort = Sort.unsorted();  // Si no es válido, no se aplicará ningún orden
        }

        // Crea el objeto Pageable que se utilizará para la paginación y ordenamiento
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);


        // Llama al repositorio para buscar productos de acuerdo con los filtros y la paginación
        return productoRepository.buscarProductosPaginado(nombre, categoriaId, precioMin, precioMax, disponibles, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> obtenerProductosPorCategoriaPaginado(Long categoriaId, int pagina, int tamanio) {

        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());
        Page<Producto> productos =  productoRepository.findByCategoriaId(categoriaId,  pageable);
        log.info("Consultando productos por categoria, paginados");

        return productos.map(productoMapper::toDto);

    }

    @Override
    @Transactional
    public ProductoDTO consultarProductoYRecomendar(Long productoId, Long usuarioId) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        // Incrementar veces visto
        producto.setVecesVisto(producto.getVecesVisto() + 1);
       Producto productoGuardado = productoRepository.save(producto);

        HistorialNavegacion historial = HistorialNavegacion.builder()
                .idUsuario(usuarioId)
                .idProducto(productoId)
                .build();

        // Registrar historial
        historialNavegacionRepository.save(historial);

        return productoMapper.toDto(productoGuardado);
    }

    @Override
    @Transactional
    public Page<ProductoDTO> consultarRecomendacionesUltimoProductoUsuarioVisto( Long usuarioId, int pagina, int tamanio) {

        Long ultimoProductoId = historialNavegacionRepository
                .findUltimosVistosPorUsuario(usuarioId)
                .stream()
                .findFirst()
                .orElseThrow(null);

        Producto producto = productoRepository
                .findById(ultimoProductoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());

        Long categoriaId = producto.getCategoria().getId();

        Page<ProductoDTO> productos = productoRepository
                .findByCategoriaId(categoriaId, pageable)
                .map(productoMapper::toDto);

        return productos;
    }

}
