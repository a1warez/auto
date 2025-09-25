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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository; // Нужен для получения пользователя

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Cart getCartByUser(Long userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        if (cartOptional.isPresent()) {
            return cartOptional.get();
        } else {
            // Создаем новую корзину, если она еще не существует
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        }
    }

    public void addProductToCart(Long userId, Long productId, int quantity) {
        Cart cart = getCartByUser(userId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (existingItemOptional.isPresent()) {
            CartItem existingItem = existingItemOptional.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
            // Обновляем связь в корзине (важно для правильного управления коллекциями)
            cart.getCartItems().add(newItem);
            cartRepository.save(cart); // Сохраняем корзину с добавленным элементом
        }
    }

    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (quantity > 0) {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        } else {
            // Удаляем товар из корзины, если количество <= 0
            cartItemRepository.deleteById(cartItemId);
        }
    }

    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public void clearCart(Long userId) {
        Cart cart = getCartByUser(userId);
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    public int getCartItemCount(Long userId) {
        Cart cart = getCartByUser(userId);
        return cart.getCartItems().size();
    }
}