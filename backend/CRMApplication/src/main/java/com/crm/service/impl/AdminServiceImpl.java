package com.crm.service.impl;

import com.crm.exception.ResourceNotFoundException;
import com.crm.model.*;
import com.crm.model.User.UserStatus;
import com.crm.repository.*;
import com.crm.service.AdminService;
import com.crm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired private UserRepository userRepository;
    @Autowired private EmailCampaignRepository emailCampaignRepository;
    @Autowired private CustomerCampaignRepository customerCampaignRepository;
    @Autowired private InteractionRepository interactionRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private SettingsRepository settingsRepository;
    @Autowired private EmailService emailService;

    @Override
    public User approveCustomer(Long customerId) {
        User customer = userRepository.findById(customerId)
                .filter(user -> user.getStatus() == UserStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Pending customer not found with id: " + customerId));
        
        customer.setStatus(UserStatus.ACTIVE);
        User updatedCustomer = userRepository.save(customer);
        
        String subject = "Your Account has been Approved!";
        String htmlBody = buildAccountApprovalEmail(updatedCustomer.getUsername());
        emailService.sendHtmlMessage(updatedCustomer.getEmail(), subject, htmlBody);
        
        return updatedCustomer;
    }
    
    private String buildAccountApprovalEmail(String username) {
        return "<!DOCTYPE html>" +
               "<html lang='en'>" +
               "<head><style>" +
               "  body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
               "  .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }" +
               "  .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #dddddd; }" +
               "  .header img { max-width: 120px; margin-bottom: 10px; }" +
               "  .content { padding: 20px 0; line-height: 1.6; color: #333333; }" +
               "  .content h1 { color: #27ae60; }" +
               "  .button-container { text-align: center; margin-top: 20px; }" +
               "  .button { background-color: #3498db; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; }" +
               "  .footer { text-align: center; font-size: 12px; color: #777777; padding-top: 20px; border-top: 1px solid #dddddd; }" +
               "</style></head>" +
               "<body>" +
               "<div class='container'>" +
               "  <div class='header'>" +
               // **MODIFIED**: Using a professional and working image link
               "    <img src='https://i.imgur.com/5Q2F2Hq.png' alt='Account Approved'>" +
               "    <h1>Welcome Aboard!</h1>" +
               "  </div>" +
               "  <div class='content'>" +
               "    <p>Hello " + username + ",</p>" +
               "    <p>Great news! Your account with the Customer Management System has been reviewed and approved by an administrator. You can now log in and access your dashboard.</p>" +
               "    <div class='button-container'>" +
               "      <a href='http://localhost:5173/login' class='button'>Login to Your Account</a>" +
               "    </div>" +
               "  </div>" +
               "  <div class='footer'>" +
               "    <p>&copy; " + LocalDateTime.now().getYear() + " CRM Project. All rights reserved.</p>" +
               "  </div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    @Override
    public Page<User> getAllCustomers(Pageable pageable) {
        return userRepository.findByRole(Role.ROLE_CUSTOMER, pageable);
    }
    
    @Override
    public User addCustomer(User customer) {
        if (customer.getPassword() == null || customer.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required for a new customer.");
        }
        customer.setRole(Role.ROLE_CUSTOMER);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setStatus(UserStatus.ACTIVE);
        customer.setJoinDate(LocalDateTime.now());
        return userRepository.save(customer);
    }

    @Override
    public User updateCustomer(Long id, User customerDetails) {
        User customer = userRepository.findById(id)
            .filter(user -> user.getRole() == Role.ROLE_CUSTOMER)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        
        customer.setUsername(customerDetails.getUsername());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAge(customerDetails.getAge());
        customer.setAdharCard(customerDetails.getAdharCard());
        customer.setAddress(customerDetails.getAddress());
        customer.setStatus(customerDetails.getStatus());

        return userRepository.save(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        User customer = userRepository.findById(id)
            .filter(user -> user.getRole() == Role.ROLE_CUSTOMER)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        interactionRepository.deleteByCustomerId(id);
        notificationRepository.deleteByUserId(id);
        customerCampaignRepository.deleteByCustomerId(id);

        userRepository.delete(customer);
    }
    
    @Override
    public Page<User> getPendingCustomers(Pageable pageable) {
        return userRepository.findByRoleAndStatus(Role.ROLE_CUSTOMER, UserStatus.PENDING, pageable);
    }
    
    @Override
    public void rejectCustomer(Long customerId) {
        User customer = userRepository.findById(customerId)
                .filter(user -> user.getStatus() == UserStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Pending customer not found with id: " + customerId));
        
        String customerEmail = customer.getEmail();
        userRepository.delete(customer);
        
        emailService.sendSimpleMessage(customerEmail, "Account Update", "We regret to inform you that your registration for the CRM Portal has been rejected.");
    }

    @Override
    public List<CustomerCampaign> getPendingCampaigns() {
        return customerCampaignRepository.findByStatus("PENDING");
    }

    @Override
    public CustomerCampaign updateCustomerCampaignStatus(Long campaignId, String status) {
        CustomerCampaign campaign = customerCampaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer Campaign not found: " + campaignId));
        
        campaign.setStatus(status.toUpperCase());
        campaign.setReviewedAt(LocalDateTime.now());
        
        Notification notification = new Notification();
        notification.setUser(campaign.getCustomer());
        String message = String.format("Your campaign proposal '%s' has been %s by the admin.", campaign.getTitle(), status.toLowerCase());
        notification.setMessage(message);
        notificationRepository.save(notification);

        return customerCampaignRepository.save(campaign);
    }

    @Override
    public Page<Interaction> getPendingInteractions(Pageable pageable) {
        return interactionRepository.findByStatus("PENDING", pageable);
    }

    @Override
    public Interaction updateInteractionStatus(Long interactionId, String status) {
        Interaction interaction = interactionRepository.findById(interactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Interaction not found with id: " + interactionId));

        interaction.setStatus(status.toUpperCase());
        Interaction updatedInteraction = interactionRepository.save(interaction);

        Notification notification = new Notification();
        notification.setUser(interaction.getCustomer());
        String message = String.format(
            "Admin has reviewed your interaction '%s'. New status: %s",
            interaction.getSubject(),
            status.toLowerCase()
        );
        notification.setMessage(message);
        notificationRepository.save(notification);

        return updatedInteraction;
    }
    
    @Override
    public Map<String, Object> getAdminAnalytics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", userRepository.countByRole(Role.ROLE_CUSTOMER));
        stats.put("activeCustomers", userRepository.countByRoleAndStatus(Role.ROLE_CUSTOMER, UserStatus.ACTIVE));
        stats.put("totalInteractions", interactionRepository.count());
        stats.put("conversionRate", 68); // Placeholder

        List<Object[]> monthlyCounts = userRepository.countCustomersByMonth();
        List<Map<String, Object>> customerGrowthData = new ArrayList<>();
        long cumulativeCount = 0;

        for (Object[] row : monthlyCounts) {
            Integer year = (Integer) row[0];
            Integer month = (Integer) row[1];
            long countInMonth = (long) row[2];
            cumulativeCount += countInMonth;
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("date", String.format("%d-%02d", year, month));
            monthData.put("count", cumulativeCount);
            customerGrowthData.add(monthData);
        }
        stats.put("customerGrowth", customerGrowthData);

        return stats;
    }

    @Override
    public Page<EmailCampaign> getAllCampaigns(Pageable pageable) {
        return emailCampaignRepository.findAll(pageable);
    }

    @Override
    public EmailCampaign createCampaign(EmailCampaign campaign) {
        campaign.setCreatedAt(LocalDateTime.now());
        if (campaign.getStatus() == null) {
            campaign.setStatus("draft");
        }
        return emailCampaignRepository.save(campaign);
    }

    @Override
    public EmailCampaign updateCampaign(Long id, EmailCampaign campaignDetails) {
        EmailCampaign campaign = emailCampaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Email Campaign not found: " + id));
        
        campaign.setName(campaignDetails.getName());
        campaign.setSubject(campaignDetails.getSubject());
        campaign.setStatus(campaignDetails.getStatus());
        return emailCampaignRepository.save(campaign);
    }

    @Override
    public void deleteCampaign(Long id) {
        if (!emailCampaignRepository.existsById(id)) {
            throw new ResourceNotFoundException("Email Campaign not found: " + id);
        }
        emailCampaignRepository.deleteById(id);
    }

    @Override
    public User getAdminProfile(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + username));
    }

    @Override
    public User updateAdminProfile(String username, User userDetails) {
        User admin = getAdminProfile(username);
        admin.setEmail(userDetails.getEmail());
        admin.setPhone(userDetails.getPhone());
        admin.setDepartment(userDetails.getDepartment());
        admin.setPosition(userDetails.getPosition());
        admin.setBio(userDetails.getBio());
        return userRepository.save(admin);
    }

    @Override
    public void changeAdminPassword(String username, Map<String, String> passwordData) {
        User admin = getAdminProfile(username);
        if (!passwordEncoder.matches(passwordData.get("currentPassword"), admin.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }
        admin.setPassword(passwordEncoder.encode(passwordData.get("newPassword")));
        userRepository.save(admin);
    }

    @Override
    public Settings getSystemSettings() {
        return settingsRepository.findById(1L).orElseGet(() -> {
            Settings newSettings = new Settings();
            newSettings.setId(1L);
            newSettings.setGeneralSettings("{}");
            newSettings.setEmailSettings("{}");
            newSettings.setSecuritySettings("{}");
            return settingsRepository.save(newSettings);
        });
    }

    @Override
    public Settings updateSystemSettings(Settings settings) {
        settings.setId(1L); 
        return settingsRepository.save(settings);
    }
}

