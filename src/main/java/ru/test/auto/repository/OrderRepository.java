package ru.test.auto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.test.auto.model.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Находит все заказы, принадлежащие определенному пользователю,
     * отсортированные по дате заказа в порядке убывания (самые новые первыми).
     *
     * @param userId ID пользователя.
     * @return Список заказов.
     */
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
}