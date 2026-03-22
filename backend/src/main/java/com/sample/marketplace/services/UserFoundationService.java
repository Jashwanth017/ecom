package com.sample.marketplace.services;

import com.sample.marketplace.models.BuyerProfile;
import com.sample.marketplace.models.SellerProfile;
import com.sample.marketplace.models.User;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.UserStatus;
import com.sample.marketplace.repositories.BuyerProfileRepository;
import com.sample.marketplace.repositories.SellerProfileRepository;
import com.sample.marketplace.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserFoundationService {

    private final UserRepository userRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;

    public UserFoundationService(
            UserRepository userRepository,
            BuyerProfileRepository buyerProfileRepository,
            SellerProfileRepository sellerProfileRepository
    ) {
        this.userRepository = userRepository;
        this.buyerProfileRepository = buyerProfileRepository;
        this.sellerProfileRepository = sellerProfileRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByEmailAndRole(String email, Role role) {
        return userRepository.findByEmailAndRole(email, role);
    }

    @Transactional(readOnly = true)
    public boolean accountExists(String email, Role role) {
        return userRepository.existsByEmailAndRole(email, role);
    }

    @Transactional(readOnly = true)
    public BuyerProfile getBuyerProfileByUserId(Long userId) {
        return buyerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer profile not found for user id " + userId));
    }

    @Transactional(readOnly = true)
    public SellerProfile getSellerProfileByUserId(Long userId) {
        return sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Seller profile not found for user id " + userId));
    }

    public User createBuyerUser(String email, String passwordHash, String fullName, String phone) {
        User user = userRepository.save(User.create(email, passwordHash, Role.BUYER, UserStatus.ACTIVE));
        buyerProfileRepository.save(BuyerProfile.create(user, fullName, phone));
        return user;
    }

    public User createSellerUser(String email, String passwordHash, String storeName, String storeDescription) {
        User user = userRepository.save(User.create(email, passwordHash, Role.SELLER, UserStatus.ACTIVE));
        sellerProfileRepository.save(SellerProfile.create(user, storeName, storeDescription));
        return user;
    }

    public User createAdminUser(String email, String passwordHash) {
        return userRepository.save(User.create(email, passwordHash, Role.ADMIN, UserStatus.ACTIVE));
    }
}
