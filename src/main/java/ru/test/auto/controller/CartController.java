package ru.test.auto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.test.auto.model.Cart;
import ru.test.auto.model.CartItem;
import ru.test.auto.model.Product;
import ru.test.auto.model.User;
import ru.test.auto.service.CartService;
import ru.test.auto.service.ProductService;
import ru.test.auto.service.UserService;


import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public CartController(CartService cartService, UserService userService, ProductService productService) {
        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Long productId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Необходимо войти в систему.");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден.");
        }

        Optional<Product> productOptional = productService.getProductById(productId); // Получаем Optional<Product>

        if (!productOptional.isPresent()) { // Проверяем, что продукт найден
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Товар не найден.");
        }

        Product product = productOptional.get(); // Извлекаем Product из Optional<Product>

        cartService.addItemToCart(user, product, 1); // Добавляем товар в корзину
        return ResponseEntity.ok("Товар добавлен в корзину.");
    }


    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        System.out.println("VIEW CART CALLED"); // Добавьте этот лог

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            System.out.println("User not authenticated, redirecting to login.");
            return "redirect:/login";
        }

        String username = authentication.getName();
        System.out.println("Authenticated user: " + username); // Лог

        User user = userService.findByUsername(username);

        if (user == null) {
            System.out.println("User not found for username: " + username); // Лог
            model.addAttribute("errorMessage", "Пользователь не найден.");
            return "error";
        }

        System.out.println("User found: " + user.getUsername()); // Лог

        List<CartItem> cartItems = cartService.getCartItemsForUser(user);
        System.out.println("Number of cart items retrieved: " + cartItems.size()); // Лог
        for (CartItem item : cartItems) {
            System.out.println("  Item: " + item.getProduct().getName() + ", Quantity: " + item.getQuantity()); // Лог
        }


        double total = cartItems.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);

        return "cart";
    }
}