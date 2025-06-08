package com.unconv.spring.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unconv_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnconvRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    @NotEmpty(message = "Role name cannot be empty")
    private String name;

    @Column(nullable = false)
    private String createdReason;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    // TODO Remove this when tests pass
    public UnconvRole(UUID uuid, String name) {
        this.id = uuid;
        this.name = name;
    }
}
