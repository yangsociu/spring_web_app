// Note: Repository cho PaymentInfo entity
package com.gamehub.repository;

import com.gamehub.model.PaymentInfo;
import com.gamehub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {

    Optional<PaymentInfo> findByIsActiveTrue();

    Optional<PaymentInfo> findByUserAndIsActiveTrue(User user);

    List<PaymentInfo> findByUser(User user);

    List<PaymentInfo> findByUserIsNull();

    Optional<PaymentInfo> findByUserIsNullAndIsActiveTrue();

    // Sửa để tìm system payment info bằng user cụ thể (thay vì user null)
    @Query("SELECT p FROM PaymentInfo p WHERE p.user.email = 'admin@example.com' AND p.isActive = true")
    Optional<PaymentInfo> findSystemActivePaymentInfo();

    @Query("SELECT p FROM PaymentInfo p WHERE p.user.email = 'admin@example.com'")
    List<PaymentInfo> findSystemPaymentInfos();

}
