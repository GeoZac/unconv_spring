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

    /**
     * Creates an {@code UnconvRole} using the provided parameters without applying any defaults.
     * The {@code origin} class's fully qualified name is used for both {@code createdReason} and
     * {@code createdBy}. This method assumes that all arguments are non-null; no null checks or
     * fallbacks are applied.
     *
     * @param id the UUID to assign to the role
     * @param roleName the name of the role
     * @param origin the class representing the creation origin
     * @return a new {@code UnconvRole} instance with values populated directly from inputs
     * @throws NullPointerException if {@code origin} is {@code null}
     */
    public static UnconvRole create(UUID id, String roleName, Class<?> origin) {
        String originName = origin.getName();
        return new UnconvRole(id, roleName, originName, originName, new Date());
    }
}
