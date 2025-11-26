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
                .csrf(csrf -> csrf.disable()) // Tắt CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Kích hoạt CORS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // Cho phép tất cả API (Demo)
                );

        return http.build();
    }

    // --- CẤU HÌNH CORS CHUẨN ĐỂ NHẬN COOKIE ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Dùng setAllowedOriginPatterns("*") thay vì liệt kê cứng
        // Cái này nghĩa là: "Mọi trang web đều được phép gọi tôi"
        // Spring sẽ tự động lấy Origin của người gọi và điền vào Header trả về -> Cookie sẽ đi qua được.
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 2. Cho phép các method
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 3. Cho phép mọi header (Authorization, Content-Type, X-Requested-With...)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 4. QUAN TRỌNG: Cho phép gửi Cookie
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}