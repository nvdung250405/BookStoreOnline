package com.bookstore.service;

import com.bookstore.dto.ChangePasswordRequest;
import com.bookstore.dto.CustomerProfileRequest;
import com.bookstore.dto.StaffProfileRequest;
import com.bookstore.dto.AccountProfileDTO;
import com.bookstore.entity.Customer;
import com.bookstore.entity.Staff;
import com.bookstore.entity.Account;
import com.bookstore.repository.CustomerRepository;
import com.bookstore.repository.StaffRepository;
import com.bookstore.repository.AccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, 
                       CustomerRepository customerRepository, 
                       StaffRepository staffRepository,
                       PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    @Transactional(readOnly = true)
    public AccountProfileDTO getCurrentUserProfile() {
        String username = getCurrentUsername();

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account information not found"));

        AccountProfileDTO profileDTO = new AccountProfileDTO();
        profileDTO.setUsername(account.getUsername());
        profileDTO.setRole(account.getRole());

        if ("CUSTOMER".equalsIgnoreCase(account.getRole())) {
            Customer customer = customerRepository.findByAccount_Username(username).orElse(null);
            if (customer != null) {
                profileDTO.setFullName(customer.getFullName());
                profileDTO.setPhone(customer.getPhone());
                profileDTO.setShippingAddress(customer.getShippingAddress());
                profileDTO.setLoyaltyPoints(customer.getLoyaltyPoints());
            }
        } else {
            Staff staff = staffRepository.findByAccount_Username(username).orElse(null);
            if (staff != null) {
                profileDTO.setFullName(staff.getFullName());
                profileDTO.setPhone(staff.getPhone());
                profileDTO.setDepartment(staff.getDepartment());
            }
        }

        return profileDTO;
    }

    @Transactional
    public AccountProfileDTO updateCustomerProfile(CustomerProfileRequest request) {
        String username = getCurrentUsername();
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found"));

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setShippingAddress(request.getShippingAddress());
        customerRepository.save(customer);

        return getCurrentUserProfile();
    }

    @Transactional
    public AccountProfileDTO updateStaffProfile(StaffProfileRequest request) {
        String username = getCurrentUsername();
        Staff staff = staffRepository.findByAccount_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Staff profile not found"));

        staff.setFullName(request.getFullName());
        staff.setPhone(request.getPhone());
        staffRepository.save(staff);

        return getCurrentUserProfile();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = getCurrentUsername();
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password confirmation does not match");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    @Transactional
    public AccountProfileDTO createCustomerProfile(CustomerProfileRequest request) {
        String username = getCurrentUsername();
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!"CUSTOMER".equalsIgnoreCase(account.getRole())) {
            throw new IllegalArgumentException("Account is not a customer");
        }

        if (customerRepository.findByAccount_Username(username).isPresent()) {
            throw new IllegalArgumentException("Customer profile already exists");
        }

        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setShippingAddress(request.getShippingAddress());
        customerRepository.save(customer);

        return getCurrentUserProfile();
    }
}
