package ru.test.auto.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.test.auto.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Lazy
    private UserService userService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/login", "/register", "/products/**", "/cart/**", "/checkout").permitAll() // Разрешаем доступ к страницам логина, регистрации, продуктам, корзине и оформлению заказа
                .antMatchers("/my-orders/**").authenticated() // Только аутентифицированные пользователи могут просматривать свои заказы
                .antMatchers("/admin/**").hasRole("ADMIN") // Административные страницы доступны только ADMIN
                .anyRequest().permitAll() // Разрешаем доступ ко всем остальным запросам (ОЧЕНЬ ВАЖНО: это должно быть в конце)
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/") // Перенаправлять на главную страницу после успешного входа
                .failureUrl("/login?error=true") // Добавлено для отображения ошибки при некорректном вводе
                .permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // Для поддержки POST запроса на выход
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedPage("/access-denied"); // Страница для запрещенного доступа
    }
}