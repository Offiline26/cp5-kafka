package br.com.fiap.rastreamentoentregaskafka.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webhookClient(WebClient.Builder builder) {
        return builder.build();
    }
}

