package com.unconv.spring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @Column(nullable = false)
    private String createdReason;

    @JsonIgnore
    @Column(nullable = false)
    private String createdBy;

    @JsonIgnore
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

    public static UnconvRole create(UUID id, String roleName, Class<?> origin) {
        String originName = origin != null ? origin.getName() : "UnknownOrigin";
        return new UnconvRole(
                id != null ? id : UUID.randomUUID(),
                roleName != null ? roleName : "Default Role",
                originName,
                originName,
                new Date());
    }
}
