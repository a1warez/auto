package ru.test.auto.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.test.auto.model.User;

import java.util.Optional; // Лучше возвращать Optional для поиска по ID

@Repository // Помечаем класс как Spring-репозиторий
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по его имени пользователя.
     * Spring Data JPA автоматически создаст реализацию этого метода,
     * основываясь на его названии.
     *
     * @param username Имя пользователя для поиска.
     * @return User (или null, если пользователь не найден).
     */
    User findByUsername(String username);

    /**
     * Находит пользователя по его email.
     *
     * @param email Email пользователя для поиска.
     * @return User (или null, если пользователь не найден).
     */
    User findByEmail(String email);

    /**
     * Находит пользователя по его имени пользователя, возвращая Optional.
     * Это более безопасный подход, чем возвращать User напрямую,
     * так как позволяет явно обработать случай, когда пользователь не найден.
     *
     * @param username Имя пользователя для поиска.
     * @return Optional, содержащий User, если найден, иначе пустой Optional.
     */
    Optional<User> findOptionalByUsername(String username);
}