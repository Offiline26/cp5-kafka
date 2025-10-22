package br.com.fiap.rastreamentoentregaskafka.model;

public class RotaAtualizada {
    public String entregaId;
    public Status status;
    public long estimativaEntregaEpochMs;
    public long timestamp;
    public RotaAtualizada(String entregaId, Status status, long eta, long ts) {
        this.entregaId = entregaId; this.status = status; this.estimativaEntregaEpochMs = eta; this.timestamp = ts;
    }

    public String getEntregaId() {
        return entregaId;
    }

    public void setEntregaId(String entregaId) {
        this.entregaId = entregaId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getEstimativaEntregaEpochMs() {
        return estimativaEntregaEpochMs;
    }

    public void setEstimativaEntregaEpochMs(long estimativaEntregaEpochMs) {
        this.estimativaEntregaEpochMs = estimativaEntregaEpochMs;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
