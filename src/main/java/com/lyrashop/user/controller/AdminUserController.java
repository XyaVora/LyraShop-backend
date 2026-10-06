package com.lyrashop.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.jpa.domain.Specification;

import com.lyrashop.user.dto.AdminUserResponse;
import com.lyrashop.user.dto.AdminUserDetailResponse;
import com.lyrashop.user.dto.UpdateUserRoleRequest;
import com.lyrashop.user.dto.UpdateUserStatusRequest;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;
import com.lyrashop.user.service.UserNotFoundException;
import com.lyrashop.user.service.UserRoleService;
import com.lyrashop.user.service.LastAdminException;
import com.lyrashop.user.service.SelfAdminMutationException;
import com.lyrashop.user.entity.UserRole;
import com.lyrashop.user.repository.LoyaltyAccountRepository;
import com.lyrashop.order.repository.ShopOrderRepository;
import com.lyrashop.order.dto.OrderResponse;
import com.lyrashop.common.dto.PageResponse;
import com.lyrashop.common.web.AdminPageable;
import com.lyrashop.address.dto.AddressResponse;
import com.lyrashop.address.repository.ShippingAddressRepository;
import com.lyrashop.user.dto.CustomerSupportNoteRequest;
import com.lyrashop.user.dto.CustomerSupportNoteResponse;
import com.lyrashop.user.entity.CustomerSupportNote;
import com.lyrashop.user.repository.CustomerSupportNoteRepository;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserRepository users;
    private final UserRoleService userRoleService;
    private final ShopOrderRepository orders;
    private final LoyaltyAccountRepository loyaltyAccounts;
    private final ShippingAddressRepository addresses;
    private final CustomerSupportNoteRepository supportNotes;

    public AdminUserController(UserRepository users, UserRoleService userRoleService,
            ShopOrderRepository orders, LoyaltyAccountRepository loyaltyAccounts,
            ShippingAddressRepository addresses, CustomerSupportNoteRepository supportNotes) {
        this.users = users;
        this.userRoleService = userRoleService;
        this.orders = orders;
        this.loyaltyAccounts = loyaltyAccounts;
        this.addresses = addresses;
        this.supportNotes = supportNotes;
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORDER_MANAGER','SUPPORT')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminUserResponse> list() {
        return users.findAll().stream().map(AdminUserResponse::from).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORDER_MANAGER','SUPPORT')")
    @GetMapping(path = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<AdminUserResponse> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Specification<User> specification = (root, ignored, builder) -> builder.conjunction();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.strip().toLowerCase(java.util.Locale.ROOT) + "%";
            specification = specification.and((root, ignored, builder) -> builder.or(
                    builder.like(builder.lower(root.get("email")), pattern),
                    builder.like(builder.lower(root.get("fullName")), pattern),
                    builder.like(builder.lower(root.get("phone")), pattern)));
        }
        if (role != null) specification = specification.and((root, ignored, builder) -> builder.equal(root.get("role"), role));
        if (active != null) specification = specification.and((root, ignored, builder) -> builder.equal(root.get("active"), active));
        var result = users.findAll(specification, AdminPageable.of(
                page, size, sort, direction,
                java.util.Set.of("email", "fullName", "phone", "role", "active", "createdAt", "updatedAt"),
                "createdAt")).map(AdminUserResponse::from);
        return PageResponse.from(result, result.getContent());
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORDER_MANAGER','SUPPORT')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminUserDetailResponse get(@PathVariable String id) {
        UUID userId;
        try { userId = UUID.fromString(id); }
        catch (IllegalArgumentException exception) { throw new UserNotFoundException(); }
        User user = users.findById(userId).orElseThrow(UserNotFoundException::new);
        var loyalty = loyaltyAccounts.findById(userId).orElse(null);
        return new AdminUserDetailResponse(
                AdminUserResponse.from(user),
                loyalty == null ? 0 : loyalty.getCoinBalance(),
                loyalty == null ? 0 : loyalty.getCoinDebt(),
                orders.sumPaidTotalByUserId(userId),
                orders.findAllByUserIdOrderByCreatedAtDescIdDesc(userId).stream().map(OrderResponse::from).toList(),
                addresses.findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId).stream().map(AddressResponse::from).toList(),
                supportNotes.findAllByCustomerIdOrderByCreatedAtDesc(userId).stream()
                        .map(note -> CustomerSupportNoteResponse.from(note,
                                users.findById(note.getCreatedBy()).map(User::getFullName).orElse("Nhân viên")))
                        .toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORDER_MANAGER','SUPPORT')")
    @PostMapping(path = "/{id}/support-notes", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerSupportNoteResponse addSupportNote(Authentication authentication, @PathVariable String id,
            @Valid @RequestBody CustomerSupportNoteRequest request) {
        UUID customerId;
        try { customerId = UUID.fromString(id); }
        catch (IllegalArgumentException exception) { throw new UserNotFoundException(); }
        users.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID createdBy = UUID.fromString(authentication.getName());
        CustomerSupportNote saved = supportNotes.saveAndFlush(CustomerSupportNote.create(customerId, createdBy, request.note()));
        String createdByName = users.findById(createdBy).map(User::getFullName).orElse("Nhân viên");
        return CustomerSupportNoteResponse.from(saved, createdByName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminUserResponse updateStatus(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UUID userId;
        try {
            userId = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new UserNotFoundException();
        }
        if (userId.equals(UUID.fromString(authentication.getName()))) {
            throw new SelfAdminMutationException();
        }
        User user = users.findById(userId).orElseThrow(UserNotFoundException::new);
        if (!Boolean.TRUE.equals(request.active()) && user.getRole() == UserRole.ADMIN
                && user.isActive() && users.countByRoleAndActiveTrue(UserRole.ADMIN) <= 1) {
            throw new LastAdminException();
        }
        if (Boolean.TRUE.equals(request.active())) {
            user.activate();
        } else {
            user.deactivate();
        }
        return AdminUserResponse.from(users.saveAndFlush(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}/role", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminUserResponse updateRole(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        UUID userId;
        try {
            userId = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new UserNotFoundException();
        }
        if (userId.equals(UUID.fromString(authentication.getName()))) {
            throw new SelfAdminMutationException();
        }
        return AdminUserResponse.from(userRoleService.assignRole(userId, request.role()));
    }
}
