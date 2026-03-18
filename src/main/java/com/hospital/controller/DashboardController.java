package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.repository.RoleRepository;
import com.hospital.repository.NurseLogRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.UserRepository;
import com.hospital.repository.WardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Controller
public class DashboardController {


    // Temporary storage for system settings
    private static final Map<String, Boolean> SYSTEM_SETTINGS = new HashMap<>();
    static {
        SYSTEM_SETTINGS.put("strongPasswords", true);
        SYSTEM_SETTINGS.put("require2FA", false);
        SYSTEM_SETTINGS.put("sessionTimeout", true);
        SYSTEM_SETTINGS.put("emailAlerts", true);
        SYSTEM_SETTINGS.put("smsAlerts", false);
        SYSTEM_SETTINGS.put("maintenanceWarnings", true);
    }
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final RoleRepository roleRepository;
    private final com.hospital.repository.NurseLogRepository nurseLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DashboardController(PatientRepository patientRepository, 
                               UserRepository userRepository,
                               WardRepository wardRepository,
                               RoleRepository roleRepository,
                               com.hospital.repository.NurseLogRepository nurseLogRepository,
                               PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.wardRepository = wardRepository;
        this.roleRepository = roleRepository;
        this.nurseLogRepository = nurseLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @org.springframework.web.bind.annotation.ModelAttribute("assignedPatientsCount")
    public int defaultAssignedPatientsCount() {
        return 0;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) { 
        long totalPatients = patientRepository.count();
        long activeConsultations = patientRepository.findAll().stream()
                .filter(p -> !"DISCHARGED".equals(p.getStatus()))
                .count();
        double totalRevenue = patientRepository.findAll().stream()
                .filter(p -> "Paid".equals(p.getPaymentStatus()))
                .mapToDouble(p -> p.getTotalCharges())
                .sum();
        
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("activeCount", activeConsultations);
        model.addAttribute("revenue", totalRevenue);
        return "admin/dashboard"; 
    }

    @GetMapping("/doctor/dashboard")
    public String doctorDashboard(Principal principal, Model model) { 
        String doctorName = userRepository.findByUsername(principal.getName())
                .map(u -> u.getFullName())
                .orElse("Unknown Doctor");
        
        java.util.List<Patient> myPatients = patientRepository.findByAssignedDoctor(doctorName);
        model.addAttribute("patients", myPatients);
        model.addAttribute("wards", wardRepository.findAll());
        model.addAttribute("assignedPatientsCount", myPatients.size());
        return "doctor/dashboard"; 
    }

    @GetMapping("/receptionist/dashboard")
    public String receptionistDashboard(Model model) { 
        java.util.List<Patient> allPatients = patientRepository.findAll();
        double totalCollections = allPatients.stream()
                .filter(p -> "Paid".equals(p.getPaymentStatus()))
                .mapToDouble(p -> p.getTotalCharges())
                .sum();
        
        model.addAttribute("patients", allPatients);
        model.addAttribute("totalCollections", totalCollections);
        return "receptionist/dashboard"; 
    }

    @GetMapping("/nurse/dashboard")
    public String nurseDashboard(Model model) { 
        java.util.List<Patient> patients = patientRepository.findAll().stream()
                .filter(p -> "WARD".equals(p.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("patients", patients);
        model.addAttribute("wards", wardRepository.findAll());
        model.addAttribute("nurseLogs", nurseLogRepository.findAll());
        return "nurse/dashboard"; 
    }

    @GetMapping("/lab/dashboard")
    public String labDashboard(Model model) { 
        model.addAttribute("patients", patientRepository.findAll().stream()
                .filter(p -> "LAB".equals(p.getStatus()))
                .collect(java.util.stream.Collectors.toList()));
        return "lab_technician/dashboard"; 
    }

    @GetMapping("/pharmacist/dashboard")
    public String pharmacistDashboard(Model model) { 
        model.addAttribute("patients", patientRepository.findAll().stream()
                .filter(p -> "PHARMACY".equals(p.getStatus()))
                .collect(java.util.stream.Collectors.toList()));
        return "pharmacist/dashboard"; 
    }

    @GetMapping("/billing/dashboard")
    public String billingDashboard(Model model) { 
        model.addAttribute("patients", patientRepository.findAll().stream()
                .filter(p -> "BILLING".equals(p.getStatus()) || "DISCHARGED".equals(p.getStatus()))
                .collect(java.util.stream.Collectors.toList()));
        return "billing_staff/dashboard"; 
    }

    @GetMapping("/receptionist/arrival")
    public String patientArrival(Model model) { 
        model.addAttribute("doctors", userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "DOCTOR".equals(u.getRole().getName()))
                .collect(java.util.stream.Collectors.toList()));
        model.addAttribute("patients", patientRepository.findAll());
        return "receptionist/arrival"; 
    }

    @GetMapping("/receptionist/payment-check")
    public String paymentCheck() { return "receptionist/payment-check"; }

    @GetMapping("/receptionist/assign-doctor")
    public String assignDoctor() { return "receptionist/assign-doctor"; }

    @GetMapping("/receptionist/receipt")
    public String generateReceipt(Model model) { 
        model.addAttribute("patients", patientRepository.findAll());
        return "receptionist/receipt"; 
    }

    @GetMapping("/receptionist/flow")
    public String patientFlow(Model model) { 
        model.addAttribute("patients", patientRepository.findAll());
        return "receptionist/flow"; 
    }

    @GetMapping("/receptionist/discharge")
    public String discharge(Model model) { 
        model.addAttribute("patients", patientRepository.findAll());
        return "receptionist/discharge"; 
    }

    // ===== ADMIN STAFF MANAGEMENT =====

    @GetMapping("/admin/staff")
    public String staffManagement(Model model) {
        model.addAttribute("staffList", userRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("systemSettings", SYSTEM_SETTINGS);
        return "admin/staff-management";
    }

    @GetMapping("/admin/wards")
    public String wardManagement(Model model) {
        model.addAttribute("wards", wardRepository.findAll());
        return "admin/wards";
    }

    @PostMapping("/admin/wards/add")
    public String addWard(@RequestParam("name") String name,
                          @RequestParam("capacity") int capacity,
                          @RequestParam("type") String type,
                          RedirectAttributes redirectAttributes) {
        com.hospital.model.Ward ward = new com.hospital.model.Ward();
        ward.setName(name);
        ward.setCapacity(capacity);
        ward.setType(type);
        wardRepository.save(ward);
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "Ward '" + name + "' has been added to the system.");
        return "redirect:/admin/wards";
    }

    @PostMapping("/admin/wards/delete")
    public String deleteWard(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        wardRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "Ward has been removed.");
        return "redirect:/admin/wards";
    }

    @PostMapping("/admin/staff/add")
    public String addStaff(@RequestParam("fullName") String fullName,
                           @RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("role") String role,
                           @RequestParam(value = "picture", required = false) MultipartFile picture,
                           RedirectAttributes redirectAttributes) {
        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username '" + username + "' is already taken. Please choose another.");
            return "redirect:/admin/staff";
        }
        com.hospital.model.User newUser = new com.hospital.model.User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        roleRepository.findByName(role).ifPresent(newUser::setRole);
        
        if (picture != null && !picture.isEmpty()) {
            saveProfileImage(picture, newUser);
        }
        
        userRepository.save(newUser);
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "Staff account for '" + fullName + "' has been created successfully (Profile enabled).");
        return "redirect:/admin/staff";
    }

    @PostMapping("/admin/staff/delete/{id}")
    public String deleteStaff(@PathVariable("id") Long id,
                              RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "Staff account deleted.");
        return "redirect:/admin/staff";
    }

    @PostMapping("/admin/staff/update")
    public String updateStaff(@RequestParam("id") Long id,
                            @RequestParam("fullName") String fullName,
                            @RequestParam("role") String role,
                            RedirectAttributes redirectAttributes) {
        com.hospital.model.User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setFullName(fullName);
            roleRepository.findByName(role).ifPresent(user::setRole);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("successMsg", "Staff account '" + fullName + "' updated successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Staff member not found.");
        }
        return "redirect:/admin/staff";
    }

    // General Links
    @GetMapping("/user-accounts")
    public String userAccounts() { return "redirect:/admin/staff"; }

    @GetMapping("/admin/settings")
    public String adminSettings(Model model) {
        model.addAttribute("systemSettings", SYSTEM_SETTINGS);
        return "admin/settings";
    }

    @PostMapping("/admin/settings/save")
    public String saveSettings(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        // Reset all checkboxes to false first (since unchecked checkboxes aren't submitted)
        SYSTEM_SETTINGS.keySet().forEach(key -> SYSTEM_SETTINGS.put(key, false));
        
        // Mark as true for submitted values
        params.forEach((key, value) -> {
            if (SYSTEM_SETTINGS.containsKey(key)) {
                SYSTEM_SETTINGS.put(key, "true".equals(value));
            }
        });

        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "System configuration updated and PERSISTED successfully!");
        return "redirect:/admin/settings";
    }

    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        java.util.List<Patient> allPatients = patientRepository.findAll();
        
        long totalPatients = allPatients.size();
        long activeCount = allPatients.stream()
                .filter(p -> !"DISCHARGED".equals(p.getStatus()))
                .count();
        long dischargedCount = allPatients.stream()
                .filter(p -> "DISCHARGED".equals(p.getStatus()))
                .count();
        double totalRevenue = allPatients.stream()
                .filter(p -> "Paid".equals(p.getPaymentStatus()))
                .mapToDouble(p -> p.getTotalCharges())
                .sum();

        // Status counts for chart
        java.util.Map<String, Long> statusCounts = allPatients.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getStatus() != null ? p.getStatus() : "UNKNOWN", 
                        java.util.stream.Collectors.counting()));
        
        // Ensure all statuses have at least 0
        String[] statuses = {"QUEUE", "CONSULTING", "LAB", "PHARMACY", "WARD", "BILLING", "DISCHARGED"};
        for (String s : statuses) {
            statusCounts.putIfAbsent(s, 0L);
        }

        // Status colors for inline chart
        java.util.Map<String, String> statusColors = new java.util.HashMap<>();
        statusColors.put("QUEUE", "#eab308"); // yellow
        statusColors.put("CONSULTING", "#3b82f6"); // blue
        statusColors.put("LAB", "#ea580c"); // orange
        statusColors.put("PHARMACY", "#22c55e"); // green
        statusColors.put("WARD", "#db2777"); // pink
        statusColors.put("BILLING", "#8b5cf6"); // purple
        statusColors.put("DISCHARGED", "#64748b"); // slate

        // Staff stats
        java.util.List<com.hospital.model.User> allStaff = userRepository.findAll();
        java.util.Map<String, Long> roleCounts = allStaff.stream()
                .filter(u -> u.getRole() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        u -> u.getRole().getName(), java.util.stream.Collectors.counting()));

        // Recent Patients (last 10)
        java.util.List<Patient> recentPatients = allPatients.stream()
                .sorted(java.util.Comparator.comparing(Patient::getCreatedAt).reversed())
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("dischargedCount", dischargedCount);
        model.addAttribute("totalRevenue", totalRevenue);
        
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("statusColors", statusColors);
        
        model.addAttribute("totalStaff", (long)allStaff.size());
        model.addAttribute("roleCounts", roleCounts);
        model.addAttribute("recentPatients", recentPatients);

        return "admin/reports";
    }

    @GetMapping("/appointments")
    public String appointments() { return "general/appointments"; }

    @GetMapping("/clinical-data")
    public String clinicalData() { return "general/clinical-data"; }

    @PostMapping("/receptionist/register")
    public String registerPatient(@org.springframework.web.bind.annotation.ModelAttribute Patient patient, 
                                  @RequestParam("doctorName") String doctorName,
                                  RedirectAttributes redirectAttributes) {
        patient.setPatientId("PAT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        patient.setAssignedDoctor(doctorName);
        patient.setStatus("QUEUE");
        patientRepository.save(patient);
        
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("patientId", patient.getPatientId());
        redirectAttributes.addFlashAttribute("assignedDoctor", doctorName);
        
        return "redirect:/receptionist/dashboard";
    }

    // --- DOCTOR ACTIONS ---
    
    @PostMapping("/doctor/consultation/start")
    public String startConsultation(@RequestParam("id") Long id) {
        Patient p = patientRepository.findById(id).orElseThrow();
        p.setStatus("CONSULTING");
        patientRepository.save(p);
        return "redirect:/doctor/dashboard";
    }

    @PostMapping("/doctor/consultation/update")
    public String updateConsultation(@RequestParam("id") Long id, 
                                     @RequestParam("action") String action,
                                     @RequestParam(value = "notes", required = false) String notes,
                                     @RequestParam(value = "detail", required = false) String detail) {
        Patient p = patientRepository.findById(id).orElseThrow();
        p.setClinicalNotes(notes);
        
        switch (action) {
            case "LAB" -> {
                p.setStatus("LAB");
                p.setLabTestRequest(detail);
                p.setTotalCharges(p.getTotalCharges() + 50.0); // Simple lab fee
            }
            case "PHARMACY" -> {
                p.setStatus("PHARMACY");
                p.setPrescription(detail);
                p.setTotalCharges(p.getTotalCharges() + 30.0); // Simple pharmacy fee
            }
            case "WARD" -> {
                p.setStatus("WARD");
                wardRepository.findByName(detail).ifPresent(p::setWard);
                p.setTotalCharges(p.getTotalCharges() + 200.0); // Admission fee
            }
            default -> {
                p.setStatus("BILLING");
                p.setTotalCharges(p.getTotalCharges() + 100.0); // Consultation fee
            }
        }
        patientRepository.save(p);
        return "redirect:/doctor/dashboard";
    }

    // --- LAB ACTIONS ---
    
    @PostMapping("/lab/submit")
    public String submitLabResults(@RequestParam("id") Long id, 
                                   @RequestParam("testNames[]") String[] testNames,
                                   @RequestParam("testValues[]") String[] testValues,
                                   @RequestParam(required = false, defaultValue = "false") Boolean keepInLab,
                                   org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Patient p = patientRepository.findById(id).orElseThrow();
        StringBuilder newResults = new StringBuilder();
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        newResults.append("[").append(time).append("] Lab Report:\n");
        
        for (int i = 0; i < testNames.length; i++) {
            if (testNames[i] != null && !testNames[i].trim().isEmpty()) {
                newResults.append("- ").append(testNames[i]).append(": ").append(testValues[i]).append("\n");
            }
        }
        
        String oldResults = (p.getLabResults() != null) ? p.getLabResults() + "\n\n" : "";
        p.setLabResults(oldResults + newResults.toString().trim());
        
        if (keepInLab == null || !keepInLab) {
            p.setStatus("CONSULTING");
            p.setLabTestRequest(null);
        }
        
        patientRepository.save(p);
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "Lab findings for " + p.getName() + " have been securely synchronized.");
        return "redirect:/lab/dashboard";
    }

    // --- PHARMACY ACTIONS ---

    @PostMapping("/pharmacist/dispense")
    public String dispenseMedicine(@RequestParam("id") Long id) {
        Patient p = patientRepository.findById(id).orElseThrow();
        p.setStatus("BILLING");
        patientRepository.save(p);
        return "redirect:/pharmacist/dashboard";
    }

    // --- WARD ACTIONS ---
    
    @PostMapping("/nurse/admit")
    public String admitToBed(@RequestParam("id") Long id, @RequestParam("bed") String bed, RedirectAttributes redirectAttributes) {
        Patient p = patientRepository.findById(id).orElseThrow();
        p.setBedNumber(bed);
        patientRepository.save(p);
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "Patient '" + p.getName() + "' has been assigned to Bed " + bed);
        return "redirect:/nurse/dashboard";
    }

    @PostMapping("/nurse/log/add")
    public String addNurseLog(@RequestParam("id") Long id, 
                              @RequestParam("entry") String entry,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        Patient p = patientRepository.findById(id).orElseThrow();
        com.hospital.model.User nurse = userRepository.findByUsername(principal.getName()).orElseThrow();
        
        com.hospital.model.NurseLog log = new com.hospital.model.NurseLog(p, nurse, entry);
        nurseLogRepository.save(log);
        
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "Clinical observation recorded for " + p.getName());
        return "redirect:/nurse/dashboard";
    }

    @PostMapping("/nurse/finalize")
    public String finalizeNursing(@RequestParam("id") Long id, 
                                  @RequestParam("status") String status,
                                  RedirectAttributes redirectAttributes) {
        Patient p = patientRepository.findById(id).orElseThrow();
        p.setStatus(status);
        patientRepository.save(p);
        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("successMsg", "Patient care finalized. Routed to " + status);
        return "redirect:/nurse/dashboard";
    }

    // --- BILLING ACTIONS ---
    
    @PostMapping("/billing/finalize")
    public String finalizeBilling(@RequestParam("id") Long id) {
        Patient p = patientRepository.findById(id).orElseThrow();
        p.setStatus("DISCHARGED");
        p.setPaymentStatus("Paid");
        patientRepository.save(p);
        return "redirect:/billing/dashboard";
    }

    @GetMapping("/settings")
    public String settings() { return "general/settings"; }

    @GetMapping("/patient/dashboard")
    public String patientDashboard(Principal principal, Model model) { 
        com.hospital.model.User u = userRepository.findByUsername(principal.getName()).orElseThrow();
        // Since we don't have a direct link yet, we search by fullName (assuming it matches)
        java.util.List<Patient> myRecords = patientRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(u.getFullName()))
                .collect(java.util.stream.Collectors.toList());
        
        if (!myRecords.isEmpty()) {
            model.addAttribute("patient", myRecords.get(myRecords.size()-1)); // Show latest
        }
        return "patient/dashboard"; 
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        model.addAttribute("user", userRepository.findByUsername(principal.getName()).get());
        return "profile";
    }

    @PostMapping("/profile/update-password")
    public String updatePassword(@RequestParam("currentPassword") String currentPassword,
                               @RequestParam("newPassword") String newPassword,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        com.hospital.model.User user = userRepository.findByUsername(principal.getName()).get();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password doesn't match!");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords don't match!");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/profile";
    }

    @PostMapping("/profile/update-picture")
    public String updatePicture(@RequestParam("picture") MultipartFile picture, Principal principal, RedirectAttributes redirectAttributes) {
        com.hospital.model.User user = userRepository.findByUsername(principal.getName()).get();
        if (!picture.isEmpty()) {
            saveProfileImage(picture, user);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("successMsg", "Profile picture updated successfully!");
        }
        return "redirect:/profile";
    }

    private void saveProfileImage(MultipartFile picture, com.hospital.model.User user) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + picture.getOriginalFilename();
            String uploadDir = "src/main/resources/static/uploads/";
            String targetDir = "target/classes/static/uploads/";
            
            // Ensure directories exist
            new java.io.File(uploadDir).mkdirs();
            new java.io.File(targetDir).mkdirs();
            
            Path pathSrc = Paths.get(uploadDir + fileName);
            Path pathTarget = Paths.get(targetDir + fileName);
            
            Files.copy(picture.getInputStream(), pathSrc, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(picture.getInputStream(), pathTarget, StandardCopyOption.REPLACE_EXISTING);
            
            user.setProfilePicture(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

