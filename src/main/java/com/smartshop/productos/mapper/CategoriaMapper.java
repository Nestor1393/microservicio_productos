package com.smartshop.productos.mapper;

import com.smartshop.productos.dto.CategoriaCreateDTO;
import com.smartshop.productos.dto.CategoriaDTO;
import com.smartshop.productos.dto.CategoriaUpdateDTO;
import com.smartshop.productos.dto.ProductoUpdateDTO;
import com.smartshop.productos.entity.Categoria;
import com.smartshop.productos.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    // Método para mapear una Entidad Categoria a un CategoriaDTO
    // para exponer los datos al cliente (usuario).
    @Mapping(target = "subcategorias", source = "subcategorias")
    CategoriaDTO toDTO(Categoria categoria);

    //YList<CategoriaDTO> toDTOList(List<Categoria> categorias);

    //Categoria toEntity(CategoriaDTO dto);

    // 🆕 Método para mapear desde CategoriaCreateDTO a la entidad
    Categoria toEntity(CategoriaCreateDTO createDTO);

    // ⬅️ evita que se sobrescriba el id

    // ⬅️@MappingTarget mapea sobre la instancia existente
    // es decir al metodo se le pasará como parametro la categoria existente para ser actualziada.
    //@MappingTarget Categoria categoria: este es el objeto existente que se va a actualizar. MapStruct lo modificará directamente.
    @Mapping(target = "id", ignore = true)
    void actualizarDesdeDTO(CategoriaUpdateDTO dto, @MappingTarget Categoria categoria);
    //Categoria toEntityFromUpdateDTO(CategoriaUpdateDTO dto);
}
