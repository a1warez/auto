package ru.test.auto.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.test.auto.model.Product;
import ru.test.auto.service.ProductService;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ProductService productService;


    @Autowired
    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(name = "sort", defaultValue = "name") String sortField,
            @RequestParam(name = "direction", defaultValue = "asc") String sortDir,
            @RequestParam(name = "page", defaultValue = "0") int pageNo,
            @RequestParam(name = "size", defaultValue = "10") int pageSize,
            Model model) {

        try {
            // Если сортировка по цене, то по умолчанию - по убыванию
            if (sortField.equalsIgnoreCase("price") && sortDir.equalsIgnoreCase("asc")) {
                sortDir = "desc";
            }

            Sort sort = Sort.by(sortField);
            sort = sortDir.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();

            Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

            Page<Product> page = productService.getAllProducts(pageable);

            model.addAttribute("products", page.getContent());
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);

            // Вычисляем reverseSortDir в Java-коде
            String reverseSortDir = sortDir.equalsIgnoreCase("asc") ? "desc" : "asc";
            model.addAttribute("reverseSortDir", reverseSortDir);

            // Создаем URL для сортировки по имени
            String nameSortUrl = "/?sort=name&direction=";
            if (sortField.equalsIgnoreCase("name")) { //Если уже сортируем по имени
                nameSortUrl += reverseSortDir; //То меняем направление
            } else {
                nameSortUrl += "asc"; //Иначе ставим по возрастанию
            }
            model.addAttribute("nameSortUrl", nameSortUrl);

            // Создаем URL для сортировки по цене
            String priceSortUrl = "/?sort=price&direction=";
            if (sortField.equalsIgnoreCase("price")) { //Если уже сортируем по цене
                priceSortUrl += reverseSortDir; //То меняем направление
            } else {
                priceSortUrl += "desc"; //Иначе ставим по убыванию
            }
            model.addAttribute("priceSortUrl", priceSortUrl);

            return "index";

        } catch (Exception e) {
            // Логируем ошибку
            System.err.println("Error in HomeController.home: " + e.getMessage());
            // Выбрасываем исключение с HTTP статусом 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort parameters", e);
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleBadRequest(ResponseStatusException ex, Model model) {
        model.addAttribute("errorMessage", ex.getReason());
        return "error"; // Или другая страница для отображения ошибки
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
