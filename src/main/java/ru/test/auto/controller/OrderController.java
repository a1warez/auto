package ru.test.auto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.test.auto.model.CartItem;
import ru.test.auto.model.Order;
import ru.test.auto.model.OrderItem;
import ru.test.auto.model.User;
import ru.test.auto.service.CartService;
import ru.test.auto.service.OrderService;
import ru.test.auto.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    private CartService cartService;
    private final OrderService orderService;
    private final UserService userService; // Опционально: для предзаполнения данных пользователя

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    // Страница оформления заказа (checkout)
    @GetMapping("/checkout")
    public String checkout(Authentication authentication, Model model) {
        System.out.println("Checkout method called!");
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Long userId = ((ru.test.auto.model.User) authentication.getPrincipal()).getId();
        // Можно загрузить данные пользователя для предзаполнения формы адреса, если они есть
        // model.addAttribute("user", userService.loadUserByUsername(authentication.getName()));
        return "checkout"; // Thymeleaf шаблон для оформления заказа
    }

//    // Обработка оформления заказа (POST запрос)
//    @PostMapping("/checkout")
//    public String placeOrder(@RequestParam String shippingAddress, Authentication authentication, RedirectAttributes redirectAttributes) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
//        Long userId = ((ru.test.auto.model.User) authentication.getPrincipal()).getId();
//
//        try {
//            Order placedOrder = orderService.placeOrder(userId, shippingAddress);
//            redirectAttributes.addFlashAttribute("message", "Заказ успешно оформлен! Ваш номер заказа: " + placedOrder.getId());
//            return "redirect:/order-success?orderId=" + placedOrder.getId();
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//            return "redirect:/checkout"; // Возвращаемся на страницу оформления, если есть ошибка (например, нет товара на складе)
//        }
//    }

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


    @PostMapping("/checkout") // Или URL, который вы используете
    public String checkout(Model model, Authentication authentication) {
        System.out.println("Метод checkout вызван!"); // Добавьте этот лог

        // Проверка аутентификации
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            System.out.println("Пользователь не аутентифицирован, перенаправление на логин.");
            return "redirect:/login";
        }

        String username = authentication.getName();
        System.out.println("Аутентифицированный пользователь: " + username);

        User user = userService.findByUsername(username);

        if (user == null) {
            System.out.println("Пользователь не найден.");
            model.addAttribute("errorMessage", "Пользователь не найден.");
            return "error"; // Или ваша страница ошибки
        }

        // Получаем товары из корзины пользователя
        List<CartItem> cartItems = cartService.getCartItemsForUser(user);

        if (cartItems.isEmpty()) {
            System.out.println("Корзина пуста.");
            model.addAttribute("errorMessage", "Ваша корзина пуста.");
            return "cart"; // Или ваша страница корзины
        }

        // Создаем заказ
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(java.time.LocalDateTime.now());

        // Преобразуем List<CartItem> в List<OrderItem>
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    // Преобразуем цену продукта из CartItem в BigDecimal
                    BigDecimal productPrice = BigDecimal.valueOf(cartItem.getProduct().getPrice());
                    orderItem.setPrice(productPrice); // Предполагаем, что у OrderItem есть поле price типа BigDecimal

                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems); // Устанавливаем orderItems

        // Рассчитываем общую сумму заказа
        double total = cartItems.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
        order.setTotalAmount(BigDecimal.valueOf(total)); // Преобразуем double в BigDecimal

        try {
            Order savedOrder = orderService.saveOrder(order);
            System.out.println("Заказ успешно создан с ID: " + savedOrder.getId());

            // Очистка корзины (или другой код для обработки заказа)
            cartService.clearCart(user); // Если нужно очистить корзину

            model.addAttribute("order", savedOrder);
            // Перенаправляем на страницу подтверждения
            return "redirect:/order/confirmation"; // или "/order/success"
        } catch (Exception e) {
            System.err.println("Ошибка при создании заказа: " + e.getMessage());
            model.addAttribute("errorMessage", "Ошибка при создании заказа: " + e.getMessage());
            return "cart"; // Или ваша страница корзины
        }
    }

    @GetMapping("/order/confirmation") // Страница подтверждения заказа
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
        return "order-confirmation"; // Название вашей страницы подтверждения заказа
    }
}


