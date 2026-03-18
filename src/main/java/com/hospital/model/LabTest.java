package com.hospital.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String testName;

    private String testValue;
    private LocalDateTime testDate = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "requesting_doctor_id")
    private User requestingDoctor;

    public LabTest() {}

    public LabTest(Patient patient, String testName, User requestingDoctor) {
        this.patient = patient;
        this.testName = testName;
        this.requestingDoctor = requestingDoctor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public String getTestValue() { return testValue; }
    public void setTestValue(String testValue) { this.testValue = testValue; }
    public LocalDateTime getTestDate() { return testDate; }
    public User getRequestingDoctor() { return requestingDoctor; }
    public void setRequestingDoctor(User requestingDoctor) { this.requestingDoctor = requestingDoctor; }
}
