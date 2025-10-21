package br.com.fiap.rastreamentoentregaskafka.views;

import br.com.fiap.rastreamentoentregaskafka.model.RotaAtualizada;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EstadoAtualView {

    private final Map<String, RotaAtualizada> ultimoPorEntrega = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    // Consome eventos do tópico compactado e mantém "último por entregaId"
    @KafkaListener(topics = "entregas.rotas", groupId = "ui-estado-atual", autoStartup = "true")
    public void onRota(ConsumerRecord<String, String> rec) {
        try {
            RotaAtualizada r = mapper.readValue(rec.value(), RotaAtualizada.class);
            ultimoPorEntrega.put(r.entregaId, r);
            System.out.printf("[UI] rota %s | status=%s | eta=%d | ts=%d | offset=%d%n",
                    r.entregaId, r.status, r.estimativaEntregaEpochMs, r.timestamp, rec.offset());
        } catch (Exception e) {
            System.err.println("[UI] falha ao parsear mensagem de entregas.rotas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Lista uma cópia ordenada (mais recentes primeiro) para exibir no dashboard. */
    public List<RotaAtualizada> listar() {
        return ultimoPorEntrega
                .values()
                .stream()
                .sorted(Comparator.comparingLong((RotaAtualizada r) -> r.timestamp).reversed())
                .toList();
    }

    /** Utilitário opcional para testes. */
    public void clear() { ultimoPorEntrega.clear(); }

    public void aplicarLocal(RotaAtualizada r) {
        ultimoPorEntrega.put(r.entregaId, r);
    }
}
