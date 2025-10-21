package br.com.fiap.rastreamentoentregaskafka.service;

import br.com.fiap.rastreamentoentregaskafka.model.RotaAtualizada;
import br.com.fiap.rastreamentoentregaskafka.model.SlaEstourado;
import br.com.fiap.rastreamentoentregaskafka.model.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SlaService {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();
    public SlaService(KafkaTemplate<String, String> kafka) { this.kafka = kafka; }

    @KafkaListener(topics = "entregas.rotas", groupId = "sla-service")
    public void onRota(ConsumerRecord<String, String> rec) {
        try {
            RotaAtualizada r = mapper.readValue(rec.value(), RotaAtualizada.class);
            long agora = System.currentTimeMillis();
            boolean estourado = r.status != Status.ENTREGUE && agora > r.estimativaEntregaEpochMs;
            if (estourado) {
                SlaEstourado alerta = new SlaEstourado(r.entregaId, r.estimativaEntregaEpochMs, agora,
                        "Passou do horário estimado e ainda não foi ENTREGUE");
                String json = mapper.writeValueAsString(alerta);
                kafka.send("entregas.sla", r.entregaId, json);
                System.out.println("[SLA] ALERTA → " + json);
            } else {
                System.out.printf("[SLA] OK entregaId=%s status=%s%n", r.entregaId, r.status);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
