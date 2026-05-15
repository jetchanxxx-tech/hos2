package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospital_gateway_sync")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HospitalGatewaySync extends BaseEntity {

    @Column(name = "data_type", nullable = false, length = 16)
    private String dataType;

    @Column(name = "external_id", nullable = false, length = 128)
    private String externalId;

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    @Column(name = "sync_direction", nullable = false, length = 8)
    @Builder.Default
    private String syncDirection = "INBOUND";

    @Column(name = "sync_status", nullable = false, length = 16)
    @Builder.Default
    private String syncStatus = "PENDING";

    @Column(name = "raw_payload_hash", length = 64)
    private String rawPayloadHash;

    @Column(name = "processed_json", columnDefinition = "JSON")
    private String processedJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
