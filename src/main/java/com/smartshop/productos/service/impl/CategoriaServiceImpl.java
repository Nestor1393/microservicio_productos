package com.smartshop.productos.service.impl;

import com.smartshop.productos.dto.CategoriaDTO;
import com.smartshop.productos.mapper.CategoriaMapper;
import com.smartshop.productos.repository.CategoriaRepository;
import com.smartshop.productos.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
// Lombok, permite registrar mensajes de log fácilmente.
@Slf4j
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        log.info("Listando todas las categorias");
        return categoriaRepository
                .findByCategoriaPadreIsNull()
                .stream()
                .map(categoriaMapper::toDTO)
                .toList();
    }
}
