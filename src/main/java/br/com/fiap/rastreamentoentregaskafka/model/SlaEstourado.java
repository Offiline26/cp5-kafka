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
}
