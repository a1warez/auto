package ru.test.auto.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.test.auto.model.Order;
import ru.test.auto.model.Product;
import ru.test.auto.service.OrderService;
import ru.test.auto.service.ProductService;

import java.util.Optional;

@Controller
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService; // Добавим OrderService

    public AdminController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    // Отображение формы добавления продукта (только для ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/add-product")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "add-product"; // Thymeleaf шаблон для формы добавления товара
    }

    // Обработка добавления продукта (только для ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/add-product")
    public String addProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        productService.addProduct(product);
        redirectAttributes.addFlashAttribute("message", "Товар успешно добавлен!");
        return "redirect:/admin/products"; // После добавления можно перенаправить на список продуктов
    }

    // Список всех продуктов для редактирования (с пагинацией)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products")
    public String showProductList(Model model,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<Product> productPage = productService.getAllProductsPaged(PageRequest.of(page, size));
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "admin/product-list"; // Thymeleaf шаблон для списка товаров (админ)
    }

    // Форма редактирования продукта (только для ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products/{productId}/edit")
    public String showEditProductForm(@PathVariable Long productId, Model model) {
        Optional<Product> product = productService.getProductById(productId);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "admin/edit-product"; // Thymeleaf шаблон для редактирования товара
        } else {
            model.addAttribute("message", "Товар не найден!");
            return "error";
        }
    }

    // Обработка редактирования продукта (только для ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/products/{productId}/edit")
    public String editProduct(@PathVariable Long productId, @ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        if (!productId.equals(product.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID товара не совпадает!");
            return "redirect:/admin/products";
        }

        productService.updateProduct(product);
        redirectAttributes.addFlashAttribute("message", "Товар успешно обновлен!");
        return "redirect:/admin/products"; // Перенаправляем на список товаров
    }

    // Список всех заказов (только для ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders")
    public String showOrderList(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/order-list"; // Thymeleaf шаблон для списка заказов (админ)
    }

    // Детали конкретного заказа (только для ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders/{orderId}")
    public String showOrderDetail(@PathVariable Long orderId, Model model) {
        Optional<Order> order = orderService.getOrderById(orderId);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            return "admin/order-detail"; // Thymeleaf шаблон для деталей заказа (админ)
        } else {
            model.addAttribute("errorMessage", "Заказ не найден!");
            return "error";
        }
    }

    // Изменение статуса заказа (только для ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("message", "Статус заказа успешно обновлен.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}