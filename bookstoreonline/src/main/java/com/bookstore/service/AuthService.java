package com.bookstore.service;

import com.bookstore.dto.ChangePasswordRequest;
import com.bookstore.dto.ForgotPasswordRequest;
import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.LoginResponse;
import com.bookstore.entity.Account;
import com.bookstore.repository.AccountRepository;
import com.bookstore.repository.CustomerRepository;
import com.bookstore.repository.StaffRepository;
import com.bookstore.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AccountService accountService;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;

    public AuthService(AccountRepository accountRepository, 
                        PasswordEncoder passwordEncoder, 
                        AuthenticationManager authenticationManager, 
                        JwtTokenProvider tokenProvider,
                        AccountService accountService,
                        CustomerRepository customerRepository,
                        StaffRepository staffRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.accountService = accountService;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        LoginResponse response = new LoginResponse(account.getUsername(), account.getRole(), jwt, account.getUsername());
        
        // Populate profile info based on role
        if ("CUSTOMER".equalsIgnoreCase(account.getRole())) {
            customerRepository.findByAccount_Username(account.getUsername()).ifPresent(c -> {
                response.setFullName(c.getFullName());
                response.setPhone(c.getPhone());
                response.setAddress(c.getShippingAddress());
            });
        } else {
            staffRepository.findByAccount_Username(account.getUsername()).ifPresent(s -> {
                response.setFullName(s.getFullName());
                response.setPhone(s.getPhone());
            });
        }

        return response;
    }

    @Transactional
    public LoginResponse register(LoginRequest request) {
        // 1. Check if username already exists
        if (accountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // 2. Create new account with CUSTOMER role
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole("CUSTOMER");
        account.setIsActive(true);

        // 3. Save to database
        accountRepository.save(account);

        // 4. Create default customer profile
        com.bookstore.entity.Customer customer = new com.bookstore.entity.Customer();
        customer.setAccount(account);
        customer.setFullName("MEMBER_" + account.getUsername());
        customerRepository.save(customer);

        // 5. Return registration info
        return new LoginResponse(account.getUsername(), account.getRole(), null, account.getUsername());
    }

    public void changePassword(ChangePasswordRequest request) {
        accountService.changePassword(request);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        // Simulation for now
        System.out.println(">>> Request Forgot Password for email: " + request.getEmail());
    }
}
