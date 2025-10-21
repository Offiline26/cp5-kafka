package br.com.fiap.rastreamentoentregaskafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class RastreamentoEntregasKafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RastreamentoEntregasKafkaApplication.class, args);
    }

}
