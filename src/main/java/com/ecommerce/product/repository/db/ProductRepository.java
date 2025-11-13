package com.ecommerce.product.repository.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.product.model.db.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM products p WHERE MATCH(p.title, p.description) AGAINST (:query IN BOOLEAN MODE)", countQuery = "SELECT COUNT(*) FROM products p WHERE MATCH(p.title, p.description) AGAINST (:query IN BOOLEAN MODE)", nativeQuery = true)
    Page<Product> searchByTitleAndDescription(@Param("query") String query, Pageable pageable);
}
