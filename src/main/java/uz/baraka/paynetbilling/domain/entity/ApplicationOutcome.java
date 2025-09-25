package uz.baraka.paynetbilling.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "application_outcomes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_outcome_appid_purpose", columnNames = {"application_id", "purpose"})
        },
        indexes = {
                @Index(name = "ix_outcome_app_id", columnList = "application_id")
        }
)
@Getter
@Setter
public class ApplicationOutcome {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="application_id", nullable=false, length=64)
    private String applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name="purpose", nullable=false, length=16)
    private ApplicationPurpose purpose;

    @Column(name="received_at", nullable=false)
    private OffsetDateTime receivedAt;

    @PrePersist
    void prePersist() {
        if (receivedAt == null) receivedAt = OffsetDateTime.now();
    }
}
