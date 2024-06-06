package com.s20683.wmphs.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocatedStockRepository extends JpaRepository<AllocatedStock, Integer> {
    @Query(value = "SELECT * FROM allocated_stock WHERE line_id = :lineId", nativeQuery = true)
    List<AllocatedStock> findAllByLineId(@Param("lineId") int lineId);
}
