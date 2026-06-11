package com.family.spend;

import com.family.spend.model.Member;
import com.family.spend.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class FamilySpendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FamilySpendApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://thehien204.github.io", "http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173", "http://127.0.0.1:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public CommandLineRunner initDatabase(MemberRepository memberRepository) {
        return args -> {
            // Delete old members m-1, m-2, m-3
            if (memberRepository.existsById("m-1")) {
                memberRepository.deleteById("m-1");
            }
            if (memberRepository.existsById("m-2")) {
                memberRepository.deleteById("m-2");
            }
            if (memberRepository.existsById("m-3")) {
                memberRepository.deleteById("m-3");
            }
            
            // Ensure m-4 (Phạm Thế Hiển) is seeded
            if (!memberRepository.existsById("m-4")) {
                memberRepository.save(new Member("m-4", "Phạm Thế Hiển", "Thành viên", "#a855f7", "", "", "123456"));
                System.out.println("Seeded member Phạm Thế Hiển!");
            }
        };
    }
}
