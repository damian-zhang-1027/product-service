package com.ecommerce.product.repository.db;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.product.model.db.entity.Product;

import jakarta.persistence.LockModeType;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM products p WHERE MATCH(p.title, p.description) AGAINST (:query IN BOOLEAN MODE)", countQuery = "SELECT COUNT(*) FROM products p WHERE MATCH(p.title, p.description) AGAINST (:query IN BOOLEAN MODE)", nativeQuery = true)
    Page<Product> searchByTitleAndDescription(@Param("query") String query, Pageable pageable);

    Page<Product> findBySellerAdminId(Long sellerAdminId, Pageable pageable);

    Page<Product> findBySellerAdminIdAndCategoryId(Long sellerAdminId, Long categoryId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT * FROM products p WHERE p.id IN :ids", nativeQuery = true)
    List<Product> findAllByIdInWithPessimisticWrite(@Param("ids") Collection<Long> ids);
}
