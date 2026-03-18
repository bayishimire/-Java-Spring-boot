package com.hospital.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing")
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private double amount;
    private String status = "PENDING"; // PENDING, PAID, PARTIAL
    private LocalDateTime billingDate = LocalDateTime.now();
    private String description;

    public Billing() {}

    public Billing(Patient patient, double amount, String description) {
        this.patient = patient;
        this.amount = amount;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getBillingDate() { return billingDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
