package ru.test.auto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.test.auto.model.Order;
import ru.test.auto.model.OrderItem;
import ru.test.auto.model.User;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {




    /**
     * Находит все элементы, принадлежащие определенному заказу.
     *
     * @param orderId ID заказа.
     * @return Список элементов заказа.
     */
    List<OrderItem> findByOrderId(Long orderId);
}