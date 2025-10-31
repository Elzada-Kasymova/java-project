package com.users_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 100, nullable = false)
    private String firstName;

    @Column(length = 100, nullable = false)
    private String lastName;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @ElementCollection
    @CollectionTable(name = "user_companies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "company_id")
    private List<UUID> companyIds = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public User(UUID id, String firstName, String lastName, String email, List<UUID> companyIds, Instant createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.companyIds = companyIds;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", companyIds=" + companyIds +
                ", createdAt=" + createdAt +
                '}';
    }
}
