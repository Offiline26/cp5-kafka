package br.com.fiap.rastreamentoentregaskafka.controller;

import br.com.fiap.rastreamentoentregaskafka.model.SlaEstourado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/test")
public class TesteController {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    public TesteController(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    @PostMapping("/estourar-sla")
    public String estourarSla(@RequestParam(defaultValue = "teste") String entregaId) {
        try {
            SlaEstourado alerta = new SlaEstourado(
                    entregaId.equals("teste") ? UUID.randomUUID().toString() : entregaId,
                    System.currentTimeMillis() - 60_000, // ETA passado
                    System.currentTimeMillis(),
                    "Modo de teste: SLA forçado para demonstração"
            );
            String json = mapper.writeValueAsString(alerta);
            kafka.send("entregas.sla", alerta.entregaId, json);
            System.out.println("[TEST] SLA ESTOURADO FORÇADO → " + json);
            return "SLA forçado publicado com sucesso no tópico entregas.sla";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao publicar alerta: " + e.getMessage();
        }
    }
}
