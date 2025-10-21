package br.com.fiap.rastreamentoentregaskafka.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaTopicsConfig {
    @Bean
    public NewTopic entregasRotas() {
        return TopicBuilder.name("entregas.rotas")
                .partitions(3)
                .replicas(1)
                .compact() // cleanup.policy=compact
                .build();
    }

    @Bean
    public NewTopic entregasSla() {
        return TopicBuilder.name("entregas.sla")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
