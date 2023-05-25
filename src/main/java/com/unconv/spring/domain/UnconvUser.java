package com.unconv.spring.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "unconv_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnconvUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    @NotEmpty(message = "Username cannot be empty")
    private String username;

    @Column(nullable = false)
    @NotEmpty(message = "E-mail cannot be empty")
    private String email;

    @Column(nullable = false)
    @NotEmpty(message = "Password cannot be empty")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
