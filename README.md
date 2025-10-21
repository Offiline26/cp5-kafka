# 🚚 Rastreamento de Entregas com Apache Kafka

Projeto acadêmico desenvolvido em **Java + Spring Boot + Apache Kafka**, simulando um sistema de **rastreamento de entregas em tempo real** com detecção automática de **SLA estourado**.  
O sistema segue uma **arquitetura orientada a eventos (Event-Driven Architecture)**, onde cada serviço atua como **produtor** e/ou **consumidor** de tópicos Kafka.

---

## 🧩 Visão Geral

O projeto demonstra o uso de **mensageria assíncrona** para troca de eventos entre microserviços independentes.  
Cada evento representa uma atualização de status de entrega (`COLETADO`, `A_CAMINHO`, `ENTREGUE`), e o sistema monitora os prazos (ETA) para gerar **alertas de SLA**.

Produtor (UI)
   │ envia eventos "RotaAtualizada"
   ▼
[ tópico entregas.rotas ]
   │
   ├── SLA Service → analisa atraso → publica alerta
   │       │
   │       ▼
   │   [ tópico entregas.sla ]
   │           │
   │           ▼
   │     UI (AlertasView) → exibe painel de alertas em tempo real
   │
   └── UI (EstadoAtualView) → mostra status atual das entregas

## ⚙️ Tecnologias Utilizadas
Categoria	Tecnologia
Linguagem	Java 21
Framework	Spring Boot 3.5.6
Mensageria	Apache Kafka 3.7 (via Docker)
Serialização	JSON (ObjectMapper)
Frontend	Thymeleaf + Bootstrap 5
Build Tool	Gradle
Observabilidade	Kafka UI (ProvectusLabs)
Containerização	Docker Compose

## 🧠 Conceitos Envolvidos
Arquitetura orientada a eventos
Cada mudança no sistema é propagada como um evento Kafka.

Produtores e consumidores desacoplados
Cada serviço tem sua própria Consumer Group.

View materializada
Estados são mantidos em memória para rápida leitura.

Compaction e chaveamento lógico (entregaId)
Garante ordenação e unicidade por entrega.

Assincronismo e tolerância a falhas
Reprocessamento possível via offsets.


## 🐳 Docker Compose
services:
  kafka-broker:
    image: confluentinc/cp-kafka:latest
    container_name: kafka-broker
    ports:
      - "9092:9092"
      - "9093:9093"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:29092,CONTROLLER://0.0.0.0:9093,PLAINTEXT_HOST://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-broker:29092,PLAINTEXT_HOST://host.docker.internal:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-broker:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
      CLUSTER_ID: EmptNWtoR4GGWx-BH6nGLQ
      KAFKA_ENABLE_KRAFT: "yes"
    command: >
      bash -c "
        /usr/bin/kafka-storage format --ignore-formatted --cluster-id EmptNWtoR4GGWx-BH6nGLQ --config /etc/kafka/kraft/server.properties &&
        /etc/confluent/docker/run
      "

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    ports:
      - "8090:8080"
    depends_on:
      - kafka-broker
    environment:
      KAFKA_CLUSTERS_0_NAME: local-kafka-cluster
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka-broker:29092


## 🚀 Execução do Projeto
1️⃣ Subir o Kafka e Kafka UI
docker compose up -d
Kafka: localhost:9092
Kafka UI: localhost:8090

2️⃣ Rodar a aplicação Spring Boot
./gradlew bootRun
3️⃣ Acessar a interface
👉 http://localhost:8080

💻 Interface Web
📦 Envio de Eventos
Gera um UUID

Escolhe o status (COLETADO, A_CAMINHO, ENTREGUE)

Define o ETA (timestamp futuro)

Envia para o tópico entregas.rotas

🧾 Estado Atual
Tabela da esquerda — últimas atualizações por entrega.

⚠️ Alertas de SLA
Painel da direita — mostra eventos do tópico entregas.sla (atrasos detectados).

🧪 Teste: Forçar SLA
Botão “Forçar SLA (teste)” publica manualmente um SlaEstourado no tópico — útil para demonstração.

## 📡 Endpoints REST
Método	Rota	Descrição
POST	/enviar	Envia evento RotaAtualizada
GET	/api/status	Lista status atuais das entregas
GET	/api/alertas	Lista últimos alertas de SLA
POST	/test/estourar-sla?entregaId=...	Força um alerta de SLA para teste
GET	/api/entregas/ids	Lista IDs disponíveis para teste

## 🧠 Lógica de Negócio
Serviço	Função	Tópico Kafka	Tipo
UI/Producer	Envia RotaAtualizada	entregas.rotas	Producer
SLA Service	Detecta atrasos e publica SlaEstourado	entregas.sla	Consumer + Producer
EstadoAtualView	Mantém último status por entrega	entregas.rotas	Consumer
AlertasView	Mantém últimos alertas de SLA	entregas.sla	Consumer

### 👨‍💻 Autores

Thiago Mendes do Nascimento
Vinicius Banciela
Guilherme Gonçalves Britto
Gabriel Lima
