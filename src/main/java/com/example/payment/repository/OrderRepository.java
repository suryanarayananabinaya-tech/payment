package com.example.payment.repository;

import com.example.payment.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            select o from Order o
            where (:cursorId IS NULL OR o.id< :cursorId)
            order by o.id desc
            """)
    List<Order> find(@Param("cursorId") Long cursorId, Pageable pageable);
}
