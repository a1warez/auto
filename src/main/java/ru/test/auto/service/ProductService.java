package ru.test.auto.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.test.auto.model.Product;
import ru.test.auto.repository.ProductRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // Помечаем как Spring-сервис
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) { // Конструктор
        this.productRepository = productRepository;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        System.out.println("Sorting by: " + pageable.getSort()); // Добавьте этот лог
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
//    public List<Product> searchProducts(String searchTerm) {
//        if (searchTerm == null || searchTerm.trim().isEmpty()) {
//            return getAllProducts(); // Возвращаем все продукты, если поисковый запрос пустой
//        }
//
//        String searchTermLower = searchTerm.trim().toLowerCase(); // Убираем пробелы и приводим к нижнему регистру
//
//        List<Product> products = productRepository.findByNameContainingIgnoreCase(searchTermLower);
//        products.addAll(productRepository.findByPartNumberContainingIgnoreCase(searchTermLower));
//        products.addAll(productRepository.findByBrandContainingIgnoreCase(searchTermLower));
//
//        return products.stream()
//                .distinct() // Убираем дубликаты
//                .collect(Collectors.toList());
//    }
    public Product addProduct(Product product) {
        productRepository.save(product);
        return product;
    }
    public Page<Product> getAllProductsPaged(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
        // --- НОВЫЙ МЕТОД: updateProduct ---
     @Transactional // Рекомендуется использовать @Transactional для операций, изменяющих состояние
     public void updateProduct(Product product) {
            // Проверяем, существует ли продукт с таким ID
         if (productRepository.existsById(product.getId())) {
             // Spring Data JPA автоматически обновит запись, если она существует
             // и у вас есть метод save() в репозитории, который умеет работать с ID
             productRepository.save(product);
         } else {
             // Можно выбросить исключение, если продукт не найден
             throw new RuntimeException("Product with ID " + product.getId() + " not found.");
         }
        }
    }

    // Можно добавить методы для обновления, удаления, добавления в корзину и т.д.
