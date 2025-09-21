// src/main/java/com/crm/config/DataSeeder.java
package com.crm.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.crm.model.Role;
import com.crm.model.User;
import com.crm.model.User.UserStatus;
import com.crm.repository.InteractionRepository;
import com.crm.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        if (userRepository.findByUsername("admin").isEmpty()) {
            System.out.println("No admin account found. Creating default admin...");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setEmail("admin@crmproject.com");
            admin.setRole(Role.ROLE_ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setJoinDate(LocalDateTime.now());
            admin.setDepartment("Management");
            admin.setPosition("System Administrator");
            userRepository.save(admin);
            System.out.println("Default admin account created successfully.");
            System.out.println("Username: admin | Password: password");
        } else {
            System.out.println("Admin account already exists.");
        }

        
            
        
    }
}