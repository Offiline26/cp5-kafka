package br.com.fiap.rastreamentoentregaskafka.model;

public class RotaAtualizada {
    public String entregaId;
    public Status status;
    public long estimativaEntregaEpochMs;
    public long timestamp;
    public RotaAtualizada() {}
    public RotaAtualizada(String entregaId, Status status, long eta, long ts) {
        this.entregaId = entregaId; this.status = status; this.estimativaEntregaEpochMs = eta; this.timestamp = ts;
    }
}
