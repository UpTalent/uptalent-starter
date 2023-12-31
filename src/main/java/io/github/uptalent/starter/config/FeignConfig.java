package io.github.uptalent.starter.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import java.time.Duration;
import java.util.Collection;

import static io.github.uptalent.starter.util.Constants.USER_ID_KEY;
import static io.github.uptalent.starter.util.Constants.USER_ROLE_KEY;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final HttpServletRequest request;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Collection<String> skipInterceptorHeaders = requestTemplate.headers().get("skipInterceptor");
            if (skipInterceptorHeaders == null || !Boolean.parseBoolean(skipInterceptorHeaders.iterator().next())) {
                String userId = request.getHeader(USER_ID_KEY);
                String userRole = request.getHeader(USER_ROLE_KEY);
                if (userId != null) requestTemplate.header(USER_ID_KEY, userId);
                if (userRole != null) requestTemplate.header(USER_ROLE_KEY, userRole);
            }
        };
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(10000))
                .build();
        return TimeLimiterRegistry.of(timeLimiterConfig);
    }
}