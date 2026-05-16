package com.huifu.starchain.repository;

import com.huifu.starchain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByPhoneHash(String phoneHash);
    Optional<User> findByOpenid(String openid);
    Optional<User> findByWecomId(String wecomId);
    Optional<User> findByFamilyIdAndId(Long familyId, Long id);
    List<User> findByFamilyId(Long familyId);
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    long countByRole(User.UserRole role);
    long countByStatus(User.UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since AND u.role = 'RESIDENT'")
    long countNewResidentsSince(LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.familyId IS NOT NULL AND u.role = 'RESIDENT'")
    long countWithFamily();

    @Query("SELECT u FROM User u WHERE u.phone LIKE %:keyword% OR u.nameMasked LIKE %:keyword%")
    Page<User> searchByKeyword(String keyword, Pageable pageable);
}
