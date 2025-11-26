package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF (Vì ta dùng API stateless, không dùng session form HTML)
                .csrf(csrf -> csrf.disable())

                // 2. Kích hoạt CORS theo cấu hình bên dưới
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Phân quyền truy cập
                .authorizeHttpRequests(auth -> auth
                                // Cho phép truy cập tự do vào các API Login, Register, Product list...
                                // (Trong giai đoạn dev, ta có thể mở hết để test cho dễ)
                                .requestMatchers("/**").permitAll()

                        // Hoặc nếu muốn chặt chẽ hơn:
                        // .requestMatchers("/user/login", "/product/list", "/product/images/**").permitAll()
                        // .anyRequest().authenticated()
                );

        return http.build();
    }

    // Cấu hình CORS chi tiết
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép Frontend chạy ở port 5500 (Live Server)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5500", "http://127.0.0.1:5500"));

        // Cho phép các method
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cho phép mọi header (Authorization, Content-Type...)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // QUAN TRỌNG: Cho phép gửi Cookie (vì bạn dùng withCredentials=true bên axios)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}