package com.hospital.config;

import com.hospital.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                    com.hospital.repository.WardRepository wardRepository,
                                    com.hospital.repository.RoleRepository roleRepository) {
        return args -> {
            // Seed Roles first
            if (roleRepository.count() == 0) {
                roleRepository.save(new com.hospital.model.Role("ADMIN"));
                roleRepository.save(new com.hospital.model.Role("DOCTOR"));
                roleRepository.save(new com.hospital.model.Role("NURSE"));
                roleRepository.save(new com.hospital.model.Role("RECEPTIONIST"));
                roleRepository.save(new com.hospital.model.Role("LAB_TECHNICIAN"));
                roleRepository.save(new com.hospital.model.Role("PHARMACIST"));
                roleRepository.save(new com.hospital.model.Role("BILLING_STAFF"));
            }

            createUser(userRepository, roleRepository, "admin", "admin123", "System Administrator", "ADMIN");
            createUser(userRepository, roleRepository, "doctor", "doctor123", "Dr. Gregory House", "DOCTOR");
            createUser(userRepository, roleRepository, "receptionist", "recept123", "Sarah Jenkins", "RECEPTIONIST");
            createUser(userRepository, roleRepository, "nurse", "nurse123", "Clara Barton", "NURSE");
            createUser(userRepository, roleRepository, "lab", "lab123", "Marie Curie", "LAB_TECHNICIAN");
            createUser(userRepository, roleRepository, "pharmacist", "pharm123", "Alexander Fleming", "PHARMACIST");
            createUser(userRepository, roleRepository, "billing", "bill123", "Robert Kiyosaki", "BILLING_STAFF");
            createUser(userRepository, roleRepository, "patient", "patient123", "John Doe", "PATIENT");

            // Seed Wards
            if (wardRepository.count() == 0) {
                com.hospital.model.Ward ward1 = new com.hospital.model.Ward();
                ward1.setName("General Ward A");
                ward1.setCapacity(20);
                ward1.setType("General");
                wardRepository.save(ward1);

                com.hospital.model.Ward ward2 = new com.hospital.model.Ward();
                ward2.setName("Intensive Care Unit (ICU)");
                ward2.setCapacity(5);
                ward2.setType("ICU");
                wardRepository.save(ward2);

                com.hospital.model.Ward ward3 = new com.hospital.model.Ward();
                ward3.setName("Emergency Observation");
                ward3.setCapacity(10);
                ward3.setType("Emergency");
                wardRepository.save(ward3);
            }
        };
    }

    private void createUser(UserRepository repo, com.hospital.repository.RoleRepository roleRepo, String user, String pass, String name, String roleName) {
        com.hospital.model.User existing = repo.findByUsername(user).orElse(new com.hospital.model.User());
        existing.setUsername(user);
        existing.setPassword(passwordEncoder.encode(pass));
        existing.setFullName(name);
        roleRepo.findByName(roleName).ifPresent(existing::setRole);
        repo.save(existing);
    }
}
