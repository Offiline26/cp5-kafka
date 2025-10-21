package br.com.fiap.rastreamentoentregaskafka.views;

import br.com.fiap.rastreamentoentregaskafka.model.SlaEstourado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class AlertasView {

    private static final int LIMITE = 50;

    private final ConcurrentLinkedDeque<SlaEstourado> ultimos = new ConcurrentLinkedDeque<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "entregas.sla", groupId = "ui-alertas", autoStartup = "true")
    public void onAlerta(ConsumerRecord<String, String> rec) {
        try {
            SlaEstourado a = mapper.readValue(rec.value(), SlaEstourado.class);

            // adiciona no topo (mais recente primeiro)
            ultimos.addFirst(a);

            // poda para manter no máximo LIMITE
            while (ultimos.size() > LIMITE) {
                ultimos.removeLast();
            }

            System.out.printf("[UI] alerta %s | detectado=%d | eta=%d | offset=%d%n",
                    a.entregaId, a.detectadoEmEpochMs, a.estimativaEntregaEpochMs, rec.offset());

        } catch (Exception e) {
            System.err.println("[UI] falha ao parsear mensagem de entregas.sla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Retorna cópia imutável para exibição (ordem: recentes → antigos). */
    public List<SlaEstourado> listar() {
        return List.copyOf(ultimos);
    }

    /** Utilitário opcional para testes. */
    public void clear() { ultimos.clear(); }
}
