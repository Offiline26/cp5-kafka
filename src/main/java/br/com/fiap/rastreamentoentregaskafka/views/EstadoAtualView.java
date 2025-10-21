package br.com.fiap.rastreamentoentregaskafka.views;

import br.com.fiap.rastreamentoentregaskafka.model.RotaAtualizada;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;
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
            System.out.printf("[UI] rota %s | status=%s | eta=%d%n", r.entregaId, r.status, r.estimativaEntregaEpochMs);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public Collection<RotaAtualizada> listar() { return ultimoPorEntrega.values(); }

    public boolean existe(String id) { return ultimoPorEntrega.containsKey(id); }
    public RotaAtualizada get(String id) { return ultimoPorEntrega.get(id); }
    public Set<String> idsAtuais() { return new TreeSet<>(ultimoPorEntrega.keySet()); } // ordenado

    public void aplicarLocal(RotaAtualizada r) {
        ultimoPorEntrega.put(r.entregaId, r);
    }
}
