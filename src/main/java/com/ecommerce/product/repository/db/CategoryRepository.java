package com.ecommerce.product.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.product.model.db.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
