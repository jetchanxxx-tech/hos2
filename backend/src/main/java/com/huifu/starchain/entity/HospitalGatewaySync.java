package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospital_gateway_sync")
@Getter @NoArgsConstructor public class HospitalGatewaySync extends BaseEntity {

    @Column(name = "data_type", nullable = false, length = 16)
    private String dataType;

    @Column(name = "external_id", nullable = false, length = 128)
    private String externalId;

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    @Column(name = "sync_direction", nullable = false, length = 8)
        private String syncDirection = "INBOUND";

    @Column(name = "sync_status", nullable = false, length = 16)
        private String syncStatus = "PENDING";

    @Column(name = "raw_payload_hash", length = 64)
    private String rawPayloadHash;

    @Column(name = "processed_json", columnDefinition = "JSON")
    private String processedJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
        private Integer retryCount = 0;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    public HospitalGatewaySync() {}

    // ---- Getters & Setters ----
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getRawPayloadHash() { return rawPayloadHash; }
    public void setRawPayloadHash(String rawPayloadHash) { this.rawPayloadHash = rawPayloadHash; }
    public String getProcessedJson() { return processedJson; }
    public void setProcessedJson(String processedJson) { this.processedJson = processedJson; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}
