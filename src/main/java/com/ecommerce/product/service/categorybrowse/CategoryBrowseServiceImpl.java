package com.ecommerce.product.service.categorybrowse;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.controller.categorybrowse.dto.CategoryResponse;
import com.ecommerce.product.model.db.entity.Category;
import com.ecommerce.product.repository.db.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryBrowseServiceImpl implements CategoryBrowseService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }
}
