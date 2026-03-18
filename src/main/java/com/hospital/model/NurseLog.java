package com.hospital.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nurse_logs")
public class NurseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "nurse_id", nullable = false)
    private User nurse;

    @Column(columnDefinition = "TEXT")
    private String logEntry;

    private LocalDateTime logTime = LocalDateTime.now();

    public NurseLog() {}

    public NurseLog(Patient patient, User nurse, String logEntry) {
        this.patient = patient;
        this.nurse = nurse;
        this.logEntry = logEntry;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public User getNurse() { return nurse; }
    public void setNurse(User nurse) { this.nurse = nurse; }
    public String getLogEntry() { return logEntry; }
    public void setLogEntry(String logEntry) { this.logEntry = logEntry; }
    public LocalDateTime getLogTime() { return logTime; }
}
