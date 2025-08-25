package com.gamehub.repository;

import com.gamehub.model.WithdrawRequest;
import com.gamehub.model.User;
import com.gamehub.model.enums.WithdrawStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, Long> {

    Page<WithdrawRequest> findByStatus(WithdrawStatus status, Pageable pageable);

    Page<WithdrawRequest> findByUser(User user, Pageable pageable);

    Page<WithdrawRequest> findByUserAndStatus(User user, WithdrawStatus status, Pageable pageable);
}
