package ru.test.auto.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.test.auto.model.Product;

import java.util.List;

@Repository // Помечаем как Spring-репозиторий
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Пример метода для поиска по названию (Spring Data JPA генерирует реализацию)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Пример поиска по артикулу
    List<Product> findByPartNumberContainingIgnoreCase(String partNumber);

    // Пример поиска по бренду
    List<Product> findByBrandContainingIgnoreCase(String brand);

    // Поиск по совместимым авто (простая строка, для реального проекта нужен отдельный механизм)
    List<Product> findByCompatibleCarsContainingIgnoreCase(String compatibleCars);

//    Page<Product> findAll(Pageable pageable);
}