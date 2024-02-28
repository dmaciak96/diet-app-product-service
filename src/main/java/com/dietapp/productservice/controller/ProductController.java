package com.dietapp.productservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ProductController.PRODUCTS_ENDPOINT)
public class ProductController {
    public static final String PRODUCTS_ENDPOINT = "/products";


}
