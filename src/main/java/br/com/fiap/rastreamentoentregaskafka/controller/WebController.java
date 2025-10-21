package br.com.fiap.rastreamentoentregaskafka.controller;

import br.com.fiap.rastreamentoentregaskafka.views.KafkaGateway;
import br.com.fiap.rastreamentoentregaskafka.model.RotaAtualizada;
import br.com.fiap.rastreamentoentregaskafka.model.SlaEstourado;
import br.com.fiap.rastreamentoentregaskafka.model.Status;
import br.com.fiap.rastreamentoentregaskafka.views.AlertasView;
import br.com.fiap.rastreamentoentregaskafka.views.EstadoAtualView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
public class WebController {
    private final KafkaGateway kafkaGateway;
    private final EstadoAtualView estado;
    private final AlertasView alertas;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    public WebController(KafkaGateway kafkaGateway,
                         EstadoAtualView estado,
                         AlertasView alertas,
                         KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
        this.kafkaGateway = kafkaGateway;   // seu campo já existente
        this.estado = estado;
        this.alertas = alertas;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("estados", estado.listar());
        model.addAttribute("alertas", alertas.listar());
        model.addAttribute("statusValues", Status.values());
        return "index";
    }

    @PostMapping("/enviar")
    public String enviar(@RequestParam String entregaId,
                         @RequestParam Status status,
                         @RequestParam String estimativaEntregaEpochMs) {
        long eta;
        try {
            eta = Long.parseLong(estimativaEntregaEpochMs.trim());
        } catch (NumberFormatException e) {
            return "redirect:/?err=ETA_invalido";
        }

        RotaAtualizada ev = new RotaAtualizada(entregaId, status, eta, System.currentTimeMillis());
        try {
            // evita ficar pendurado: timeout curto e logs se falhar
            kafkaGateway.enviarRota(ev);
            estado.aplicarLocal(ev);// se seu gateway usa .send().get(), altere para timeout:
            // kafkaTemplate.send("entregas.rotas", entregaId, json).get(5, TimeUnit.SECONDS);
            return "redirect:/?ok=1";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/?err=Kafka";
        }
    }

    @GetMapping("/api/status")
    @ResponseBody
    public Object apiStatus() { return estado.listar(); }

    @GetMapping("/api/alertas")
    @ResponseBody
    public Object apiAlertas() { return alertas.listar(); }

    @GetMapping("/api/entregas/ids")
    @ResponseBody
    public Collection<String> idsEntregas() {
        return estado.idsAtuais();
    }

    @PostMapping("/test/estourar-sla")
    @GetMapping("/test/estourar-sla")
    @ResponseBody
    public ResponseEntity<String> forcarSla(@RequestParam String entregaId) {
        try {
            if (!estado.existe(entregaId)) {
                return ResponseEntity.badRequest().body("EntregaId não encontrada no estado atual.");
            }
            var r = estado.get(entregaId);
            long eta = r.estimativaEntregaEpochMs;
            long detectado = Math.max(System.currentTimeMillis(), eta + 1);

            SlaEstourado alerta = new SlaEstourado(
                    entregaId, eta, detectado, "Modo de teste: SLA forçado via UI");

            String json = mapper.writeValueAsString(alerta);

            var meta = kafka.send("entregas.sla", entregaId, json)
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            System.out.printf("[TEST] SLA ESTOURADO FORÇADO → %s [topic=%s, partition=%d, offset=%d]%n",
                    json, meta.getRecordMetadata().topic(), meta.getRecordMetadata().partition(), meta.getRecordMetadata().offset());

            return ResponseEntity.ok("Publicado em entregas.sla");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
}
