package ru.test.auto.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.test.auto.model.Product;
import ru.test.auto.service.ProductService;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ProductService productService;

    // Убедитесь, что ProductService правильно внедрен
    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(name = "search", required = false) String searchTerm) {
        // Получаем объект Authentication из SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Получаем имя пользователя
            String username = authentication.getName();
            model.addAttribute("username", username); // Добавляем имя пользователя в модель
        }

        List<Product> products;
        String searchResultText;

        try {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                products = productService.searchProducts(searchTerm);
                searchResultText = "Результаты поиска по запросу: \"" + searchTerm + "\"";
            } else {
                products = productService.getAllProducts();
                searchResultText = "Все товары";
            }
            model.addAttribute("products", products);
            model.addAttribute("searchResult", searchResultText);
            return "index"; // Имя Thymeleaf шаблона
        } catch (Exception e) {
            e.printStackTrace(); // Выводит полную ошибку в консоль
            model.addAttribute("errorMessage", "Произошла ошибка при загрузке товаров: " + e.getMessage());
            return "error"; // Или другой шаблон для отображения ошибки
        }
    }

    // Детальная страница товара
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        // Получаем объект Authentication из SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Получаем имя пользователя
            String username = authentication.getName();
            model.addAttribute("username", username); // Добавляем имя пользователя в модель
        }
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "product-detail"; // Thymeleaf шаблон для деталей товара
        } else {
            model.addAttribute("message", "Товар не найден!");
            return "error"; // Thymeleaf шаблон для страницы ошибки
        }
    }
}