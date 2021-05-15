package com.langthang.services.impl;

import com.langthang.dto.CategoryDTO;
import com.langthang.exception.CustomException;
import com.langthang.model.entity.Category;
import com.langthang.repository.CategoryRepository;
import com.langthang.services.ICategoryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServicesImpl implements ICategoryServices {

    @Autowired
    private CategoryRepository categoryRepo;

    @Override
    public List<CategoryDTO> getAllCategory() {

        return categoryRepo.findAll().stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }


    @Override
    public void deleteCategory(int categoryId) {
        Category category = categoryRepo.findById(categoryId).orElse(null);

        if (category == null) {
            throw new CustomException("Category with id: " + categoryId + " not found"
                    , HttpStatus.NOT_FOUND);
        }

        categoryRepo.delete(category);
    }

    @Override
    public CategoryDTO modifyCategory(int categoryId, String newName) {
        Category category = categoryRepo.findById(categoryId).orElse(null);

        if (category == null) {
            throw new CustomException("Category with id: " + categoryId + " not found"
                    , HttpStatus.NOT_FOUND);
        }

        category.setName(newName);
        Category savedCategory = categoryRepo.save(category);

        return toCategoryDTO(savedCategory);
    }

    @Override
    public CategoryDTO addNewCategory(String categoryName) {
        boolean isCategoryExist = categoryRepo.existsByName(categoryName);
        if (isCategoryExist) {
            throw new CustomException("Category with name: " + categoryName + " is already exist",
                    HttpStatus.CONFLICT);
        }

        Category newCategory = new Category(categoryName);
        categoryRepo.save(newCategory);

        return toCategoryDTO(newCategory);
    }

    private CategoryDTO toCategoryDTO(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .postCount(category.getPostCategories().size())
                .build();
    }
}
