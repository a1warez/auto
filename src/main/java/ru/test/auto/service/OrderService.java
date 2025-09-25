package ru.test.auto.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.test.auto.model.Cart;
import ru.test.auto.model.CartItem;
import ru.test.auto.model.Order;
import ru.test.auto.model.OrderItem;
import ru.test.auto.model.Product;
import ru.test.auto.model.User;
import ru.test.auto.repository.CartItemRepository;
import ru.test.auto.repository.CartRepository;
import ru.test.auto.repository.OrderRepository;
import ru.test.auto.repository.ProductRepository;
import ru.test.auto.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId); // Предполагается, что такой метод есть в OrderRepository
    }

    @Transactional
    public Order placeOrder(Long userId, String shippingAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Корзина пользователя не найдена"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Невозможно оформить пустую корзину");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(shippingAddress);
        order.setOrderStatus("NEW"); // Начальный статус заказа

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден: " + cartItem.getProduct().getId()));

            // Проверка наличия товара на складе
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Недостаточно товара на складе: " + product.getName() + ". В наличии: " + product.getStockQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);

            // Уменьшаем количество на складе
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            totalAmount = totalAmount.add(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf((long) cartItem.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Сохраняем заказ
        Order savedOrder = orderRepository.save(order);

        // Очищаем корзину пользователя после оформления заказа
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ с ID " + orderId + " не найден"));
        order.setOrderStatus(status);
        orderRepository.save(order);
    }

    // Метод для очистки корзины (если он еще не был реализован)
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getCartItems());
            cart.getCartItems().clear();
            cartRepository.save(cart);
        });
    }

    // Метод для удаления элемента из корзины
    @Transactional
    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    // Метод для обновления количества товара в корзине
    @Transactional
    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Элемент корзины не найден"));
        if (quantity > 0) {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        } else {
            // Если количество 0 или меньше, удаляем элемент
            cartItemRepository.deleteById(cartItemId);
        }
    }

    // Метод для добавления товара в корзину
    @Transactional
    public void addProductToCart(Long userId, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Если корзины нет, создаем новую
                    ru.test.auto.model.User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // Проверяем, есть ли такой товар уже в корзине
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Если есть, увеличиваем количество
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Если нет, добавляем новый элемент
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getCartItems().add(newItem); // Добавляем в коллекцию корзины
            cartItemRepository.save(newItem); // Сохраняем новый элемент
        }
        // Корзина будет сохранена автоматически через связь many-to-one с CartItem,
        // или вы можете явно вызвать cartRepository.save(cart); если нужно
    }
}