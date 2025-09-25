package ru.test.auto.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.test.auto.model.Product;
import ru.test.auto.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setStockQuantity(10);

        when(productRepository.save(product)).thenReturn(product);

        Product savedProduct = productService.addProduct(product);

        assertNotNull(savedProduct);
        assertEquals("Test Product", savedProduct.getName());
        assertEquals(100.0, savedProduct.getPrice());
        assertEquals(10, savedProduct.getStockQuantity());
    }

    @Test
    public void testGetAllProducts() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");

        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(products);

        List<Product> allProducts = productService.getAllProducts();

        assertNotNull(allProducts);
        assertEquals(2, allProducts.size());
        assertEquals("Product 1", allProducts.get(0).getName());
        assertEquals("Product 2", allProducts.get(1).getName());
    }

    @Test
    public void testGetProductById() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.getProductById(1L);

        assertTrue(foundProduct.isPresent());
        assertEquals("Test Product", foundProduct.get().getName());
    }

//    @Test
//    public void testUpdateProduct() {
//        Product product = new Product();
//        product.setId(1L);
//        product.setName("Old Name");
//
//        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
//        when(productRepository.save(product)).thenReturn(product);
//
//        product.setName("New Name");
//        Product updatedProduct = productService.updateProduct(product);
//
//        assertNotNull(updatedProduct);
//        assertEquals("New Name", updatedProduct.getName());
//    }
//
//    @Test
//    public void testDeleteProduct() {
//        Product product = new Product();
//        product.setId(1L);
//
//        doNothing().when(productRepository).deleteById(1L);
//
//        productService.deleteProduct(1L);
//
//        verify(productRepository, times(1)).deleteById(1L);
//    }
}