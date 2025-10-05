package ru.test.auto.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.test.auto.Enum.OrderStatus;
import ru.test.auto.model.*;
import ru.test.auto.service.CartService;
import ru.test.auto.service.OrderService;
import ru.test.auto.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService; // Опционально: для предзаполнения данных пользователя

    public OrderController(OrderService orderService, UserService userService, CartService cartService) {
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
    }

    @PostMapping("/checkout")
    public String checkout(Model model, RedirectAttributes redirectAttributes, Authentication authentication) {
        System.out.println("Метод checkout вызван!");

        // 1. Проверка аутентификации
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Пользователь не аутентифицирован, перенаправление на логин.");
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            System.out.println("Пользователь не найден в БД.");
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден.");
            return "redirect:/cart";
        }

        // 2. Получаем корзину
        List<CartItem> cartItems = cartService.getCartItemsForUser(user);

        if (cartItems == null || cartItems.isEmpty()) {
            System.out.println("Корзина пуста.");
            redirectAttributes.addFlashAttribute("errorMessage", "Ваша корзина пуста.");
            return "redirect:/cart";
        }

        // 3. Создание заказа
        Order newOrder = new Order();
        newOrder.setUser(user);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setOrderStatus(OrderStatus.NEW);

        try {
            // Преобразование CartItem в OrderItem и расчет цены
            List<OrderItem> orderItems = cartItems.stream()
                    .map(cartItem -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(newOrder);
                        orderItem.setProduct(cartItem.getProduct());
                        orderItem.setQuantity(cartItem.getQuantity());

                        // Обратите внимание на обработку NullPointerException и возможные другие проверки
                        BigDecimal productPrice = BigDecimal.valueOf(cartItem.getProduct().getPrice());
                        if (productPrice == null) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Цена продукта не может быть равна null.");
                        }

                        orderItem.setPrice(productPrice);

                        BigDecimal totalItemPrice = productPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                        orderItem.setTotalItemPrice(totalItemPrice);

                        return orderItem;
                    })
                    .collect(Collectors.toList());

            newOrder.setOrderItems(orderItems);

            // 4. Рассчитываем общую сумму заказа
            BigDecimal total = newOrder.getOrderItems().stream()
                    .map(OrderItem::getTotalItemPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            newOrder.setTotalAmount(total);

            // 5. Сохранение заказа
            Order savedOrder = orderService.saveOrder(newOrder);
            System.out.println("Заказ успешно создан с ID: " + savedOrder.getId());

            // 6. Очистка корзины (Обязательно!)
            cartService.clearCart(user);

            // 7. Перенаправление на страницу подтверждения
            return "redirect:/order/success";
        } catch (NullPointerException e) {
            System.err.println("NullPointerException при создании заказа: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: обнаружены некорректные данные при создании заказа.");
            return "redirect:/cart";
        } catch (ResponseStatusException rse) {
            System.err.println("Ошибка при создании заказа: " + rse.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", rse.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            System.err.println("Ошибка при создании заказа: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании заказа: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    // Страница успешного оформления заказа
    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam Long orderId, Model model) {
        Optional<Order> order = orderService.getOrderById(orderId);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            return "order-success"; // Thymeleaf шаблон для страницы успеха
        } else {
            model.addAttribute("errorMessage", "Заказ не найден.");
            return "error";
        }
    }

    @GetMapping("/my-orders") // Или ваш реальный URL для моих заказов
    public String viewMyOrders(Model model, Authentication authentication) {
        // Проверка на аутентификацию (как вы делали раньше)
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login"; // Или другой URL для перенаправления
        }

        // Получаем имя пользователя из объекта Authentication
        String username = authentication.getName();
        System.out.println("Authenticated user for my-orders: " + username); // Для отладки

        // Используем userService для поиска вашего собственного объекта User
        ru.test.auto.model.User currentUser = userService.findByUsername(username); // <- Вот здесь исправление

        if (currentUser == null) {
            model.addAttribute("errorMessage", "Пользователь не найден. Пожалуйста, войдите снова.");
            return "error"; // Или вернитесь на страницу логина
        }

        // Теперь используем currentUser (ваш ru.test.auto.model.User) для получения заказов
        List<Order> orders = orderService.getOrdersForUser(currentUser); // Предполагаем, что у вас есть orderService
        System.out.println("Found " + orders.size() + " orders for user: " + username); // Для отладки

        model.addAttribute("orders", orders);
        model.addAttribute("currentUser", currentUser); // Может пригодиться

        return "my-orders"; // Имя вашего Thymeleaf шаблона для моих заказов
    }

    // Просмотр деталей конкретного заказа
    @GetMapping("/my-orders/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, Model model) {
        Optional<Order> order = orderService.getOrderById(orderId);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            return "order-detail"; // Thymeleaf шаблон для деталей заказа
        } else {
            model.addAttribute("errorMessage", "Заказ не найден.");
            return "error";
        }
    }

    @GetMapping("/order/success") // Страница подтверждения заказа
    public String showConfirmation(Model model, Authentication authentication) {
        // Логика отображения страницы подтверждения заказа (например, с информацией о заказе)
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        // Получите пользователя (аналогично viewMyOrders)
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            model.addAttribute("errorMessage", "Пользователь не найден.");
            return "error";
        }
        model.addAttribute("user",user);
        return "order-success"; // Название вашей страницы подтверждения заказа
    }
}



