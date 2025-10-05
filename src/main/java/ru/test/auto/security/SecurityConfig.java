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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.test.auto.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Lazy
    private UserService userService;
    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

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
                // 1. ПУБЛИЧНЫЕ РЕСУРСЫ (всем)
                .antMatchers("/", "/login", "/register", "/products/**", "/static/**").permitAll()

                // 2. РЕСУРСЫ, ТРЕБУЮЩИЕ АУТЕНТИФИКАЦИИ (пользователям)
                .antMatchers("/cart/add/**", "/cart/**", "/checkout", "/my-orders/**").authenticated()

                // 3. АДМИН-ПАНЕЛЬ (только ADMIN)
                .antMatchers("/admin/**").hasRole("ADMIN")

                // 4. ВСЕ ОСТАЛЬНЫЕ ЗАПРОСЫ: (ВАЖНО)
                // a) Если у вас есть другие URL, требующие АУТЕНТИФИКАЦИИ, укажите их ЗДЕСЬ
                // b) Если у вас есть другие URL, требующие СПЕЦИАЛЬНЫХ РОЛЕЙ, укажите их ЗДЕСЬ

                // 5. ЗАВЕРШАЮЩЕЕ ПРАВИЛО (для тех, кто прошел все проверки выше):
                // * Если что-то не подходит ни под одно из правил выше, оно будет требовать АУТЕНТИФИКАЦИИ
                .anyRequest().authenticated()

                // ИЛИ
                // * Если нужно, чтобы вообще ВСЕ ОСТАЛЬНЫЕ запросы были разрешены (ОЧЕНЬ ОПАСНО):
                // .anyRequest().permitAll() // НЕ РЕКОМЕНДУЕТСЯ!

                .and()
                .formLogin()
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler) // Используем AuthenticationSuccessHandler
                .failureUrl("/login?error=true")
                .permitAll()
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedPage("/access-denied")
                .and()
                .csrf().disable();
    }
}
