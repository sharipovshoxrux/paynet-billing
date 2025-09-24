package uz.baraka.paynetbilling.port;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.baraka.paynetbilling.domain.entity.PaymentTransaction;
import uz.baraka.paynetbilling.domain.entity.TxnState;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTransactionId(Long transactionId);
    boolean existsByApplicationIdAndState(String applicationId, TxnState state);
    Optional<PaymentTransaction> findTopByApplicationIdOrderByCreatedAtDesc(String applicationId);
    List<PaymentTransaction> findAllByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
    @Query(value = """
        select distinct on (t.application_id) t.*
        from payment_transactions t
        where t.application_id in (:appIds)
        order by t.application_id, t.created_at desc
    """, nativeQuery = true)
    List<PaymentTransaction> findLatestByApplicationIds(@Param("appIds") Collection<String> appIds);
}

