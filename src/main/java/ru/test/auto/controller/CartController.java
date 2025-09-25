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
import ru.test.auto.model.Cart;
import ru.test.auto.service.CartService;
import ru.test.auto.service.UserService;


import java.util.Optional;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Отображение корзины
    @GetMapping("/cart")
    public String viewCart(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            // Если пользователь не аутентифицирован, перенаправляем на логин
            return "redirect:/login";
        }
        // Получаем ID текущего пользователя
        Long userId = ((ru.test.auto.model.User) authentication.getPrincipal()).getId();
        Cart cart = cartService.getCartByUser(userId);
        model.addAttribute("cart", cart);
        // Добавляем количество товаров в корзине для отображения в шапке (если нужно)
        // model.addAttribute("cartItemCount", cartService.getCartItemCount(userId));
        return "cart"; // Thymeleaf шаблон для отображения корзины
    }

    // Добавление товара в корзину ( POST запрос с формы)
    @PostMapping("/cart/add")
    public String addProductToCart(@RequestParam Long productId, @RequestParam int quantity, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Long userId = ((ru.test.auto.model.User) authentication.getPrincipal()).getId();

        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Количество товара должно быть больше нуля.");
            return "redirect:/cart";
        }

        try {
            cartService.addProductToCart(userId, productId, quantity);
            redirectAttributes.addFlashAttribute("message", "Товар добавлен в корзину!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart"; // Перенаправляем обратно в корзину
    }

    // Обновление количества товара в корзине
    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam Long cartItemId, @RequestParam int quantity, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            cartService.updateCartItemQuantity(cartItemId, quantity);
            redirectAttributes.addFlashAttribute("message", "Количество обновлено.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    // Удаление товара из корзины (GET запрос по ссылке)
    @GetMapping("/cart/remove/{cartItemId}")
    public String removeCartItem(@PathVariable Long cartItemId, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            cartService.removeCartItem(cartItemId);
            redirectAttributes.addFlashAttribute("message", "Товар удален из корзины.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    // Очистка всей корзины
    @GetMapping("/cart/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Long userId = ((ru.test.auto.model.User) authentication.getPrincipal()).getId();
        cartService.clearCart(userId);
        redirectAttributes.addFlashAttribute("message", "Корзина очищена.");
        return "redirect:/cart";
    }
}