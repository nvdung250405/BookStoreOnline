package com.bookstore.service;

import com.bookstore.dto.AdminCreateAccountRequest;
import com.bookstore.dto.AdminUserResponseDTO;
import com.bookstore.dto.AccountProfileDTO;
import com.bookstore.entity.Customer;
import com.bookstore.entity.Staff;
import com.bookstore.entity.Account;
import com.bookstore.enums.AccountStatus;
import com.bookstore.repository.StaffRepository;
import com.bookstore.repository.AccountRepository;
import com.bookstore.repository.CustomerRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bookstore.service.AuditLogService;

@Service
@SuppressWarnings("null")
public class AdminService {

    private final AccountRepository accountRepository;
    private final StaffRepository staffRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AdminService(AccountRepository accountRepository, 
                        StaffRepository staffRepository,
                        CustomerRepository customerRepository,
                        PasswordEncoder passwordEncoder,
                        AuditLogService auditLogService) {
        this.accountRepository = accountRepository;
        this.staffRepository = staffRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AdminUserResponseDTO createAccount(AdminCreateAccountRequest request) {
        // 1. Check existence
        if (accountRepository.existsById(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
        }

        // 2. Create new account
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(request.getRole().toUpperCase());
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        // 3. Automatically determine department based on role
        String department;
        String role = request.getRole().toUpperCase();
        department = "ADMIN".equals(role) ? "MANAGEMENT" : 
                     "STOREKEEPER".equals(role) ? "WAREHOUSE" : "SALES";

        // 4. Create profile for new staff
        Staff staff = new Staff();
        staff.setAccount(account);
        staff.setFullName("NEW STAFF MEMBER");
        staff.setDepartment(department);
        staffRepository.save(staff);

        // Log hành động
        auditLogService.log("ACCOUNT_CREATE", "Tạo tài khoản nhân viên mới: " + request.getUsername() + " (Vai trò: " + request.getRole() + ")");

        // 5. Build response DTO
        return buildAdminUserResponse(account, staff);
    }

    @Transactional
    public AdminUserResponseDTO autoCreateAccount(String role) {
        String prefix = "ADMIN".equalsIgnoreCase(role) ? "admin" :
                        "STOREKEEPER".equalsIgnoreCase(role) ? "kho" :
                        "STAFF".equalsIgnoreCase(role) ? "staff" : null;
        
        if (prefix == null) {
            throw new IllegalArgumentException("Chỉ hỗ trợ tạo tự động cho Nhân viên, Kho và Admin.");
        }
        
        // Find next index
        List<Account> existing = accountRepository.findByUsernameStartingWith(prefix);
        int maxIndex = 0;
        for (Account a : existing) {
            String u = a.getUsername();
            if (u.length() > prefix.length()) {
                try {
                    int idx = Integer.parseInt(u.substring(prefix.length()));
                    if (idx > maxIndex) maxIndex = idx;
                } catch (NumberFormatException ignored) {}
            }
        }
        String nextUsername = prefix + (maxIndex + 1);
        String defaultPassword = "123456";

        Account account = new Account();
        account.setUsername(nextUsername);
        account.setPassword(passwordEncoder.encode(defaultPassword));
        account.setRole(role.toUpperCase());
        account.setIsActive(true);
        accountRepository.save(account);

        String dept = "ADMIN".equalsIgnoreCase(role) ? "MANAGEMENT" : 
                      "STOREKEEPER".equalsIgnoreCase(role) ? "WAREHOUSE" : "SALES";
        Staff s = new Staff();
        s.setAccount(account);
        s.setFullName("NEW " + role + " " + (maxIndex + 1));
        s.setDepartment(dept);
        // Use total staff count to ensure global uniqueness for the phone column
        long totalStaff = staffRepository.count();
        s.setPhone(String.format("%010d", totalStaff + 1)); 
        staffRepository.save(s);
        
        AdminUserResponseDTO res = buildAdminUserResponse(account, s);
        res.setPassword(defaultPassword);
        return res;
    }

    private AdminUserResponseDTO buildAdminUserResponse(Account account, Staff staff) {
        AdminUserResponseDTO response = new AdminUserResponseDTO();
        response.setUsername(account.getUsername());
        response.setRole(account.getRole());
        response.setStatus(account.getStatus());
        response.setCreatedAt(account.getCreatedAt());
        if (staff != null) response.setDepartment(staff.getDepartment());
        return response;
    }


    @Transactional(readOnly = true)
    public List<AccountProfileDTO> getAllUsers() {
        // 1. Fetch all accounts
        List<Account> accounts = accountRepository.findAll();
        List<String> usernames = accounts.stream().map(Account::getUsername).collect(Collectors.toList());

        // 2. Fetch profiles in bulk
        Map<String, Customer> customerMap = customerRepository.findByAccount_UsernameIn(usernames)
                .stream()
                .collect(Collectors.toMap(kh -> kh.getAccount().getUsername(), kh -> kh));

        Map<String, Staff> staffMap = staffRepository.findByAccount_UsernameIn(usernames)
                .stream()
                .collect(Collectors.toMap(nv -> nv.getAccount().getUsername(), nv -> nv));

        // 3. Map to DTOs
        return accounts.stream()
            .map(account -> mapToAccountProfileDTO(account, customerMap, staffMap))
            .collect(Collectors.toList());
    }

    private AccountProfileDTO mapToAccountProfileDTO(Account account, Map<String, Customer> customerMap, Map<String, Staff> staffMap) {
        AccountProfileDTO dto = new AccountProfileDTO();
        dto.setUsername(account.getUsername());
        dto.setRole(account.getRole());
        dto.setIsActive(account.getIsActive());
        dto.setCreatedAt(account.getCreatedAt());

        if ("CUSTOMER".equalsIgnoreCase(account.getRole())) {
            Customer customer = customerMap.get(account.getUsername());
            if (customer != null) {
                dto.setFullName(customer.getFullName());
                dto.setPhone(customer.getPhone());
                dto.setShippingAddress(customer.getShippingAddress());
                dto.setLoyaltyPoints(customer.getLoyaltyPoints());
            }
        } else {
            Staff staff = staffMap.get(account.getUsername());
            if (staff != null) {
                dto.setFullName(staff.getFullName());
                dto.setPhone(staff.getPhone());
                dto.setDepartment(staff.getDepartment());
            }
        }
        return dto;
    }

    @Transactional
    public void updateUserStatus(String username, boolean status) {
        String currentAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentAdmin.equals(username)) {
            throw new IllegalArgumentException("You cannot lock or unlock your own account");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + username));
        
        account.setStatus(status ? AccountStatus.ACTIVE : AccountStatus.DISABLED);
        accountRepository.save(account);

        // Log hành động thay đổi trạng thái
        String actionStr = status ? "Mở khóa" : "Khóa";
        auditLogService.log("ACCOUNT_STATUS_CHANGE", actionStr + " tài khoản: " + username);
    }

    @Transactional
    public void updateUserRole(String username, String role) {
        role = role.toUpperCase();
        if (!"ADMIN".equals(role) && !"STAFF".equals(role) && !"STOREKEEPER".equals(role) && !"CUSTOMER".equals(role)) {
            throw new IllegalArgumentException("Invalid role. Only ADMIN, STAFF, STOREKEEPER, CUSTOMER are accepted.");
        }

        String currentAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentAdmin.equals(username)) {
            throw new IllegalArgumentException("You cannot change your own role");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + username));
        
        account.setRole(role);
        accountRepository.save(account);

        // Log hành động đổi vai trò
        auditLogService.log("ACCOUNT_ROLE_CHANGE", "Thay đổi vai trò của " + username + " thành " + role);
        if (!"CUSTOMER".equals(role)) {
            String department = "ADMIN".equals(role) ? "MANAGEMENT" : 
                            "STOREKEEPER".equals(role) ? "WAREHOUSE" : "SALES";
            staffRepository.findByAccount_Username(username).ifPresent(staff -> {
                staff.setDepartment(department);
                staffRepository.save(staff);
            });
        }
    }

    @Transactional
    public void updateUserProfileAdmin(String username, AccountProfileDTO dto) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // 1. Update Role if changed
        if (dto.getRole() != null && !dto.getRole().equalsIgnoreCase(account.getRole())) {
            updateUserRole(username, dto.getRole());
        }

        // 2. Update Profile Info
        if ("CUSTOMER".equalsIgnoreCase(account.getRole())) {
            Customer c = customerRepository.findByAccount_Username(username)
                    .orElseThrow(() -> new IllegalArgumentException("Customer record missing for: " + username));
            if (dto.getFullName() != null) c.setFullName(dto.getFullName());
            if (dto.getPhone() != null) c.setPhone(dto.getPhone());
            if (dto.getShippingAddress() != null) c.setShippingAddress(dto.getShippingAddress());
            customerRepository.save(c);
        } else {
            Staff s = staffRepository.findByAccount_Username(username)
                    .orElseThrow(() -> new IllegalArgumentException("Staff record missing for: " + username));
            if (dto.getFullName() != null) s.setFullName(dto.getFullName());
            if (dto.getPhone() != null) s.setPhone(dto.getPhone());
            if (dto.getDepartment() != null) s.setDepartment(dto.getDepartment());
            staffRepository.save(s);
        }
    }

    @Transactional
    public void resetPasswordAdmin(String username, String newPassword) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        // Log hành động reset mật khẩu
        auditLogService.log("ACCOUNT_PASSWORD_RESET", "Reset mật khẩu cho tài khoản: " + username);
    }
}
