package br.com.fiap.rastreamentoentregaskafka.service;

import br.com.fiap.rastreamentoentregaskafka.model.RotaAtualizada;
import br.com.fiap.rastreamentoentregaskafka.model.SlaEstourado;
import br.com.fiap.rastreamentoentregaskafka.model.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SlaService {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    // último status conhecido por entrega
    private final Map<String, RotaAtualizada> ultimo = new ConcurrentHashMap<>();
    // para não repetir alerta indefinidamente (guarda o instante que alertou)
    private final Map<String, Long> alertadoEm = new ConcurrentHashMap<>();

    public SlaService(KafkaTemplate<String, String> kafka) { this.kafka = kafka; }

    @KafkaListener(topics = "entregas.rotas", groupId = "sla-service", autoStartup = "true")
    public void onRota(ConsumerRecord<String, String> rec) {
        try {
            RotaAtualizada r = mapper.readValue(rec.value(), RotaAtualizada.class);
            ultimo.put(r.entregaId, r);

            // checagem imediata na chegada (continua tendo o comportamento atual)
            checarEAlertar(r);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // varre periodicamente para casos em que ninguém publicou nada após o ETA
    @Scheduled(fixedRate = 5000) // a cada 5s
    public void reprocessarAtrasados() {
        long agora = System.currentTimeMillis();
        for (RotaAtualizada r : ultimo.values()) {
            // só checa atrasados não entregues
            if (r.status != Status.ENTREGUE && agora > r.estimativaEntregaEpochMs) {
                checarEAlertar(r);
            }
        }
    }

    private void checarEAlertar(RotaAtualizada r) {
        long agora = System.currentTimeMillis();
        boolean estourado = r.status != Status.ENTREGUE && agora > r.estimativaEntregaEpochMs;

        if (!estourado) {
            // se entregou, limpa marca de alerta (permite novos alertas caso ETA mude no futuro)
            if (r.status == Status.ENTREGUE) {
                alertadoEm.remove(r.entregaId);
            }
            return;
        }

        // já alertou essa entrega? (evita mensagens repetidas)
        if (alertadoEm.containsKey(r.entregaId)) {
            return;
        }

        try {
            SlaEstourado alerta = new SlaEstourado(
                    r.entregaId, r.estimativaEntregaEpochMs, agora,
                    "Passou do horário estimado e ainda não foi ENTREGUE"
            );
            String json = mapper.writeValueAsString(alerta);
            kafka.send("entregas.sla", r.entregaId, json);
            alertadoEm.put(r.entregaId, agora);
            System.out.println("[SLA] ALERTA → " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
