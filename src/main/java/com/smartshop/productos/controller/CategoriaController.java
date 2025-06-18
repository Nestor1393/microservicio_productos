package com.smartshop.productos.controller;

import com.smartshop.productos.dto.CategoriaDTO;
import com.smartshop.productos.service.CategoriaService;
import com.smartshop.productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Slf4j
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping("/categorias")
    @Operation(
            summary = "Listar Categorias",
            description = "Permite consultar todas las categorias (Padre) y sus atributos"
    )
    public ResponseEntity<List<CategoriaDTO>> listarCategorias(){

        log.info("Solicitud recibida para obtener listado de Categorias.");
        List<CategoriaDTO> categorias = categoriaService.listarCategorias();

        return  ResponseEntity.ok(categorias);
    }
}
