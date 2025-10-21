package br.com.fiap.rastreamentoentregaskafka.model;

public class SlaEstourado {
    public String entregaId;
    public long estimativaEntregaEpochMs;
    public long detectadoEmEpochMs;
    public String motivo;
    public SlaEstourado() {}
    public SlaEstourado(String id, long eta, long det, String motivo) {
        this.entregaId=id; this.estimativaEntregaEpochMs=eta; this.detectadoEmEpochMs=det; this.motivo=motivo;
    }

    public String getEntregaId() {
        return entregaId;
    }

    public void setEntregaId(String entregaId) {
        this.entregaId = entregaId;
    }

    public long getEstimativaEntregaEpochMs() {
        return estimativaEntregaEpochMs;
    }

    public void setEstimativaEntregaEpochMs(long estimativaEntregaEpochMs) {
        this.estimativaEntregaEpochMs = estimativaEntregaEpochMs;
    }

    public long getDetectadoEmEpochMs() {
        return detectadoEmEpochMs;
    }

    public void setDetectadoEmEpochMs(long detectadoEmEpochMs) {
        this.detectadoEmEpochMs = detectadoEmEpochMs;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
