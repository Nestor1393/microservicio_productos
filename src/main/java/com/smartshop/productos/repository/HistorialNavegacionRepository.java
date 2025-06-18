package com.smartshop.productos.repository;

import com.smartshop.productos.entity.HistorialNavegacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialNavegacionRepository extends JpaRepository<HistorialNavegacion, Long> {

    @Query("SELECT h.idProducto FROM HistorialNavegacion h WHERE h.idUsuario = :idUsuario ORDER BY h.fecha DESC")
    List<Long> findUltimosVistosPorUsuario(@Param("idUsuario") Long idUsuario);
}
