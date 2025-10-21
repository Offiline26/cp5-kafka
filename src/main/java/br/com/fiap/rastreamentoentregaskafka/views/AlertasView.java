package br.com.fiap.rastreamentoentregaskafka.views;

import br.com.fiap.rastreamentoentregaskafka.model.SlaEstourado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;

@Component
public class AlertasView {

    private static final int MAX_ALERTAS = 50;
    private final Deque<SlaEstourado> ultimos = new ArrayDeque<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "entregas.sla", groupId = "ui-alertas") // <- grupo exclusivo da UI
    public void onAlerta(ConsumerRecord<String, String> rec) {
        try {
            String payload = rec.value();
            if (payload == null || payload.isBlank()) return;

            SlaEstourado alerta = mapper.readValue(payload, SlaEstourado.class);

            synchronized (ultimos) {
                ultimos.addFirst(alerta);
                while (ultimos.size() > MAX_ALERTAS) ultimos.removeLast();
            }

            System.out.printf("[UI] alerta SLA recebido key=%s -> %s%n", rec.key(), payload);
        } catch (Exception e) {
            System.err.println("[UI] falha ao parsear mensagem de entregas.sla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Usado por /api/alertas */
    public List<SlaEstourado> listar() {
        synchronized (ultimos) { return List.copyOf(ultimos); }
    }

    // opcional para testes
    public void clear() { synchronized (ultimos) { ultimos.clear(); } }
}
