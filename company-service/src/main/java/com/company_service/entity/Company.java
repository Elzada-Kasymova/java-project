package com.company_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    private Double budget;

    @Column(length = 100)
    private String industry;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String country;

    @ElementCollection
    @CollectionTable(name = "company_user_links", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "user_id")
    private List<UUID> userIds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Company() {}

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", budget=" + budget +
                ", industry='" + industry + '\'' +
                ", address='" + address + '\'' +
                ", country='" + country + '\'' +
                ", userIds=" + userIds +
                ", createdAt=" + createdAt +
                '}';
    }
}
