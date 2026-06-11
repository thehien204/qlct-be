package com.family.spend.repository;

import com.family.spend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    boolean existsByPaidById(String paidById);

    @Query("SELECT COUNT(e) > 0 FROM Expense e JOIN e.beneficiaryIds b WHERE b = :memberId")
    boolean existsByBeneficiaryId(@Param("memberId") String memberId);
}
