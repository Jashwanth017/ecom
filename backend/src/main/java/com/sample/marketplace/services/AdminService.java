package com.sample.marketplace.services;

import com.sample.marketplace.dto.admin.CreateCategoryRequest;
import com.sample.marketplace.dto.admin.AdminDashboardSummaryResponse;
import com.sample.marketplace.dto.admin.AdminUserResponse;
import com.sample.marketplace.models.SellerProfile;
import com.sample.marketplace.models.User;
import com.sample.marketplace.models.Category;
import com.sample.marketplace.dto.catalog.CategoryResponse;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.SellerApprovalStatus;
import com.sample.marketplace.models.enums.UserStatus;
import com.sample.marketplace.repositories.CategoryRepository;
import com.sample.marketplace.repositories.SellerProfileRepository;
import com.sample.marketplace.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;

    public AdminService(
            UserRepository userRepository,
            SellerProfileRepository sellerProfileRepository,
            CategoryRepository categoryRepository
    ) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getDashboardSummary() {
        return new AdminDashboardSummaryResponse(
                userRepository.count(),
                userRepository.countByRole(Role.BUYER),
                userRepository.countByRole(Role.SELLER),
                userRepository.countByRole(Role.ADMIN),
                userRepository.countByStatus(UserStatus.BANNED),
                sellerProfileRepository.countByApprovalStatus(SellerApprovalStatus.PENDING),
                sellerProfileRepository.countByApprovalStatus(SellerApprovalStatus.APPROVED),
                sellerProfileRepository.countByApprovalStatus(SellerApprovalStatus.REJECTED)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers(Role role, UserStatus status) {
        List<User> users = findUsers(role, status);
        return users.stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long userId) {
        User user = getUserEntity(userId);
        return toAdminUserResponse(user);
    }

    public AdminUserResponse banUser(Long userId) {
        User user = getUserEntity(userId);
        user.updateStatus(UserStatus.BANNED);
        return toAdminUserResponse(userRepository.save(user));
    }

    public AdminUserResponse unbanUser(Long userId) {
        User user = getUserEntity(userId);
        user.updateStatus(UserStatus.ACTIVE);
        return toAdminUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getPendingSellers() {
        return sellerProfileRepository.findAllByApprovalStatus(SellerApprovalStatus.PENDING).stream()
                .map(profile -> toAdminUserResponse(profile.getUser(), profile))
                .toList();
    }

    public AdminUserResponse approveSeller(Long sellerProfileId) {
        SellerProfile sellerProfile = getSellerProfileEntity(sellerProfileId);
        sellerProfile.approve();
        return toAdminUserResponse(sellerProfile.getUser(), sellerProfileRepository.save(sellerProfile));
    }

    public AdminUserResponse rejectSeller(Long sellerProfileId, String reason) {
        SellerProfile sellerProfile = getSellerProfileEntity(sellerProfileId);
        sellerProfile.reject(normalizeReason(reason));
        return toAdminUserResponse(sellerProfile.getUser(), sellerProfileRepository.save(sellerProfile));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String normalizedName = request.name().trim();
        String slug = normalizeSlug(request.slug(), normalizedName);

        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Category name already exists");
        }
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Category slug already exists");
        }

        Category category = categoryRepository.save(Category.create(normalizedName, slug));
        return toCategoryResponse(category);
    }

    private List<User> findUsers(Role role, UserStatus status) {
        if (role != null && status != null) {
            return userRepository.findAllByRoleAndStatus(role, status);
        }
        if (role != null) {
            return userRepository.findAllByRole(role);
        }
        if (status != null) {
            return userRepository.findAllByStatus(status);
        }
        return userRepository.findAll();
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found for id " + userId));
    }

    private SellerProfile getSellerProfileEntity(Long sellerProfileId) {
        return sellerProfileRepository.findById(sellerProfileId)
                .orElseThrow(() -> new EntityNotFoundException("Seller profile not found for id " + sellerProfileId));
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        if (user.getRole() == Role.SELLER) {
            SellerProfile sellerProfile = sellerProfileRepository.findByUserId(user.getId())
                    .orElse(null);
            return toAdminUserResponse(user, sellerProfile);
        }

        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                null,
                null,
                user.getCreatedAt()
        );
    }

    private AdminUserResponse toAdminUserResponse(User user, SellerProfile sellerProfile) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                sellerProfile == null ? null : sellerProfile.getApprovalStatus(),
                sellerProfile == null ? null : sellerProfile.getStoreName(),
                user.getCreatedAt()
        );
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }

    private String normalizeSlug(String requestedSlug, String name) {
        String source = requestedSlug == null || requestedSlug.isBlank() ? name : requestedSlug.trim();
        return source.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
