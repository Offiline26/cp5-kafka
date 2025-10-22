package br.com.fiap.rastreamentoentregaskafka.gateway;

import br.com.fiap.rastreamentoentregaskafka.model.RotaAtualizada;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaGateway {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    public KafkaGateway(KafkaTemplate<String, String> kafka) { this.kafka = kafka; }

    public void enviarRota(RotaAtualizada ev) throws Exception {
        String json = mapper.writeValueAsString(ev);
        kafka.send("entregas.rotas", ev.entregaId, json).get();
    }
}
