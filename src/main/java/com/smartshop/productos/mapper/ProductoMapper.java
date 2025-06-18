package com.smartshop.productos.mapper;

import com.smartshop.productos.dto.ProductoDTO;
import com.smartshop.productos.dto.ProductoCreateDTO;
import com.smartshop.productos.dto.ProductoUpdateDTO;
import com.smartshop.productos.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

//La anotación @Mapper Anota una interfaz como mapper y
// permite que Spring la detecte e inyecte automáticamente.
@Mapper(componentModel = "spring")
public interface ProductoMapper {

    //esta linea no es necearia ya que se esta utilizando @Mapper
    //ProductoMapper INSTANCE = Mappers.getMapper(ProductoMapper.class);


    //1. La anotación @Mapping mapea el nombre de la categoria que es un atributo
    // de tipo Categoria en la entidad Producto, con el nombreCategoria del destino
    //en este caso el ProductoDTO.
    //2. La siguiente linea mapea una entidad Producto a un ProductoDTO para
    // mostrar los datos al cliente (los siguientes métodos siguen la misma lógica).
    @Mapping(source = "categoria.nombre", target = "nombreCategoria")
    @Mapping(source = "imagenUrl", target = "imagenUrl")
    ProductoDTO toDto(Producto producto);

    //@Mapping(source = "nombreCategoria", target = "categoria.nombre")
    //Producto toEntity(ProductoDTO dto);
    @Mapping(source = "categoriaId", target = "categoria.id")
    Producto toEntityFromCreateDTO(ProductoCreateDTO createDTO);

    //en la anotación @Mapping, traget hace referencia al campo destino
    //al cual se le va a aplicar un valor o una regla especial de mapeo.
    @Mapping(source = "categoriaId", target = "categoria.id")
    Producto toEntityFromUpdateDTO(ProductoUpdateDTO updateDTO);
}
