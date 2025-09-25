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
import ru.test.auto.model.Order;
import ru.test.auto.service.OrderService;
import ru.test.auto.service.UserService;

import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserService userService; // Опционально: для предзаполнения данных пользователя

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    // Страница оформления заказа (checkout)
    @GetMapping("/checkout")
    public String checkout(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Long userId = ((ru.test.auto.model.User) authentication.getPrincipal()).getId();
        // Можно загрузить данные пользователя для предзаполнения формы адреса, если они есть
        // model.addAttribute("user", userService.loadUserByUsername(authentication.getName()));
        return "checkout"; // Thymeleaf шаблон для оформления заказа
    }

    // Обработка оформления заказа (POST запрос)
    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String shippingAddress, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Long userId = ((ru.test.auto.model.User) authentication.getPrincipal()).getId();

        try {
            Order placedOrder = orderService.placeOrder(userId, shippingAddress);
            redirectAttributes.addFlashAttribute("message", "Заказ успешно оформлен! Ваш номер заказа: " + placedOrder.getId());
            return "redirect:/order-success?orderId=" + placedOrder.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout"; // Возвращаемся на страницу оформления, если есть ошибка (например, нет товара на складе)
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

    // Просмотр истории заказов пользователя
    @GetMapping("/my-orders")
    public String viewMyOrders(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Long userId = ((ru.test.auto.model.User) authentication.getPrincipal()).getId();
        List<Order> orders = orderService.getOrdersByUser(userId);
        model.addAttribute("orders", orders);
        return "my-orders"; // Thymeleaf шаблон для списка заказов
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
}