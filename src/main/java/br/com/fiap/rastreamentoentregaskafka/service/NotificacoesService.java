package br.com.fiap.rastreamentoentregaskafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class NotificacoesService {

    private final WebClient client;
    private final String webhookUrl;

    public NotificacoesService(
            WebClient client,
            @Value("${app.webhook.url:http://localhost:8080/notifier/mock}") String webhookUrl) {
        this.client = client;
        this.webhookUrl = webhookUrl;
    }

    @KafkaListener(topics = "entregas.sla", groupId = "notif-service", autoStartup = "true")
    public void onAlerta(String json) {
        try {
            client.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .toBodilessEntity()
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .block();

            System.out.println("[Notif] Webhook acionado com sucesso: " + json);
        } catch (Exception e) {
            System.err.println("[Notif] Falha ao acionar webhook: " + e.getMessage());
        }
    }
}
