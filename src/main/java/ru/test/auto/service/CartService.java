package ru.test.auto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.test.auto.model.Cart;
import ru.test.auto.model.CartItem;
import ru.test.auto.model.Product;
import ru.test.auto.model.User;
import ru.test.auto.repository.CartItemRepository;
import ru.test.auto.repository.CartRepository;
import ru.test.auto.repository.ProductRepository;
import ru.test.auto.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional // Важно!
    public void addItemToCart(User user, Product product, int quantity) {
        System.out.println("addItemToCart called for user: " + user.getUsername() + ", product: " + product.getName() + ", quantity: " + quantity); // Лог

        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        System.out.println("Saving cartItem: " + cartItem.getProduct().getName() + ", quantity: " + cartItem.getQuantity()); // Лог перед сохранением
        cartItemRepository.save(cartItem); // Сохраняем cartItem
        System.out.println("CartItem saved successfully."); // Лог после сохранения
    }

    public List<CartItem> getCartItemsForUser(User user) {
        System.out.println("Fetching cart items for user: " + user.getUsername()); // Лог

        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            System.out.println("Cart not found for user: " + user.getUsername()); // Лог
            return new ArrayList<>();
        }
        System.out.println("Cart found for user: " + user.getUsername() + ", Cart ID: " + cart.getId()); // Лог

        List<CartItem> items = cartItemRepository.findByCart(cart);
        System.out.println("Found " + items.size() + " items in cart."); // Лог
        return items;
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser(user); // Находим корзину пользователя
        if (cart != null) { // Убеждаемся, что корзина существует
            List<CartItem> cartItems = cartItemRepository.findByCart(cart); // Получаем все товары в корзине
            cartItemRepository.deleteAll(cartItems); // Удаляем все товары из корзины
        }
    }
}