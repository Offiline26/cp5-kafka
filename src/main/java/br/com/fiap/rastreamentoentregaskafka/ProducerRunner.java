package br.com.fiap.rastreamentoentregaskafka;

import br.com.fiap.rastreamentoentregaskafka.model.RotaAtualizada;
import br.com.fiap.rastreamentoentregaskafka.model.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProducerRunner implements CommandLineRunner {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();
    public ProducerRunner(KafkaTemplate<String, String> kafka) { this.kafka = kafka; }

    @Override public void run(String... args) throws Exception {
        String topic = "entregas.rotas";
        List<String> entregas = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        long now = System.currentTimeMillis();

        send(topic, entregas.get(0), Status.COLETADO, now - 5_000);
        Thread.sleep(300);
        send(topic, entregas.get(0), Status.A_CAMINHO, now - 3_000);

        send(topic, entregas.get(1), Status.COLETADO, now + 60_000);
        Thread.sleep(300);
        send(topic, entregas.get(1), Status.A_CAMINHO, now + 45_000);
        Thread.sleep(300);
        send(topic, entregas.get(1), Status.ENTREGUE, now + 30_000);

        send(topic, entregas.get(2), Status.COLETADO, now + 2_000);
        Thread.sleep(2500);
        send(topic, entregas.get(2), Status.A_CAMINHO, now + 2_000);

        System.out.println("Producer: simulação enviada: " + entregas);
    }

    private void send(String topic, String entregaId, Status st, long eta) throws Exception {
        RotaAtualizada ev = new RotaAtualizada(entregaId, st, eta, System.currentTimeMillis());
        String payload = mapper.writeValueAsString(ev);
        kafka.send(topic, entregaId, payload).get();
        System.out.println("[Producer] " + payload);
    }
}
