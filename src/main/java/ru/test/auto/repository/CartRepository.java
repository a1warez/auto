package ru.test.auto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.test.auto.model.Cart;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Находит корзину по ID пользователя.
     *
     * @param userId ID пользователя.
     * @return Optional, содержащий Cart, если найден, иначе пустой Optional.
     */
    Optional<Cart> findByUserId(Long userId);
}