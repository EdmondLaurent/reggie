package com.edmond.reggie.controller;

import com.edmond.reggie.common.R;
import com.edmond.reggie.entity.Category;
import com.edmond.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类（菜品，套餐）
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("新增菜品（套餐）分类，接收的参数：{}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

}
