package br.com.fiap.rastreamentoentregaskafka.service;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("notif")
@Component
public class NotificacoesService {
    @KafkaListener(topics = "entregas.sla", groupId = "notificacoes-service")
    public void onAlerta(ConsumerRecord<String, String> rec) {
        System.out.printf("[Notif] ALERTA recebido key=%s value=%s%n", rec.key(), rec.value());
    }
}
