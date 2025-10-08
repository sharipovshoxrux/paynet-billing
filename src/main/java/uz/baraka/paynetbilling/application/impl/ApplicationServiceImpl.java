package uz.baraka.paynetbilling.application.impl;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.baraka.paynetbilling.application.ApplicationService;
import uz.baraka.paynetbilling.config.AppProps;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;
import uz.baraka.paynetbilling.domain.ApplicationStatus;
import uz.baraka.paynetbilling.domain.entity.Application;
import uz.baraka.paynetbilling.domain.entity.ApplicationOutcome;
import uz.baraka.paynetbilling.domain.entity.PaymentTransaction;
import uz.baraka.paynetbilling.domain.entity.TxnState;
import uz.baraka.paynetbilling.domain.factory.ApplicationFactory;
import uz.baraka.paynetbilling.exception.ApplicationAlreadyPaidException;
import uz.baraka.paynetbilling.exception.ApplicationAlreadyProcessedException;
import uz.baraka.paynetbilling.exception.NoSuchApplicationException;
import uz.baraka.paynetbilling.exception.ResourceAccessDeniedException;
import uz.baraka.paynetbilling.port.ApplicationOutcomeRepository;
import uz.baraka.paynetbilling.port.ApplicationRepository;
import uz.baraka.paynetbilling.port.PaymentTransactionRepository;
import uz.baraka.paynetbilling.util.IdGenerator;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository repo;
    private final ApplicationOutcomeRepository outcomeRepo;
    private final PaymentTransactionRepository txRepo;
    private final AppProps props;
    private final ApplicationFactory factory;

    @Override
    @Transactional
    public CreateResponse create(CreateRequest req) {
        Objects.requireNonNull(req, "request is required");
        if (req.userId() == null) throw new IllegalArgumentException("userId is required");
        if (req.pinfl() == null || req.pinfl().isBlank()) throw new IllegalArgumentException("pinfl is required");

        var existing = repo.findTopByUserIdAndStatusInOrderByCreatedAtDesc(
                req.userId(), List.of(ApplicationStatus.CREATED, ApplicationStatus.PAID)
        );
        if (existing.isPresent()) {
            var app = existing.get();
            return new CreateResponse(
                    app.getApplicationId(),
                    app.getName(),
                    app.getAmount(),
                    app.getSource(),
                    app.getPurpose()
            );
        }

        String applicationId = IdGenerator.generateApplicationId();

        Application app = factory.newApplication(
                applicationId,
                req.userId(),
                req.pinfl(),
                req.name(),
                props.fixedAmount(),
                req.source(),
                req.purpose(),
                req.bankType()
        );
        app.setStatus(ApplicationStatus.CREATED);

        repo.save(app);

        return new CreateResponse(
                app.getApplicationId(),
                app.getName(),
                app.getAmount(),
                app.getSource(),
                app.getPurpose()
        );
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusResponse> findStatusesByUserId(
            UUID userId,
            @Nullable ApplicationStatus statusFilter,
            @Nullable ApplicationPurpose purpose,
            @Nullable ApplicationSource source) {

        var apps = repo.findAllByUserIdAndFilters(userId, purpose, source);
        if (apps.isEmpty()) return List.of();

        var appIds = apps.stream().map(Application::getApplicationId).toList();

        var latestTxByAppId = txRepo.findLatestByApplicationIds(appIds).stream()
                .collect(Collectors.toMap(PaymentTransaction::getApplicationId, Function.identity()));

        var outcomes = outcomeRepo.findAllByApplicationIdIn(appIds).stream()
                .collect(Collectors.groupingBy(ApplicationOutcome::getApplicationId));

        return apps.stream()
                .map(app -> {
                    ApplicationStatus status;

                    if (app.getStatus() == ApplicationStatus.CANCELLED) {
                        status = ApplicationStatus.CANCELLED;
                    } else if (outcomes.containsKey(app.getApplicationId())) {
                        status = ApplicationStatus.COMPLETED;
                    } else {
                        var latestTx = latestTxByAppId.get(app.getApplicationId());
                        if (latestTx == null) {
                            status = ApplicationStatus.CREATED;
                        } else {
                            switch (latestTx.getState()) {
                                case PAID -> status = ApplicationStatus.PAID;
                                case CANCELLED -> status = ApplicationStatus.REFUNDED;
                                default -> status = ApplicationStatus.CREATED;
                            }
                        }
                    }

                    return new ApplicationStatusResponse(
                            app.getApplicationId(),
                            status,
                            app.getSource(),
                            app.getPurpose()
                    );
                })
                .filter(r -> statusFilter == null || r.status() == statusFilter)
                .toList();
    }

    @Override
    @Transactional
    public void recordOutcome(String applicationId, ApplicationPurpose purpose) {
        Objects.requireNonNull(applicationId, "applicationId is required");
        Objects.requireNonNull(purpose, "purpose is required");

        var app = repo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Клиент не найден"));

        boolean alreadyRecorded = outcomeRepo.existsByApplicationIdAndPurpose(applicationId, purpose);
        if (!alreadyRecorded) {
            var outcome = new ApplicationOutcome();
            outcome.setApplicationId(applicationId);
            outcome.setPurpose(purpose);
            outcomeRepo.save(outcome);
        }

        switch (app.getStatus()) {
            case PAID -> {
                app.setStatus(ApplicationStatus.COMPLETED);
                repo.save(app);
            }
//            case CREATED -> {
//
//            }
//            case COMPLETED, CANCELLED, REFUNDED -> {
//            }
        }
    }

    @Transactional
    public boolean markPaidByBank(String applicationId) {
        var app = repo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Заявка не найдена"));

        if (app.getSource() != ApplicationSource.BANK) {
            throw new ResourceAccessDeniedException("Доступен только для заявок от Банка");
        }

        boolean alreadyPaid = txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
        if (alreadyPaid) return true;

        var tx = new PaymentTransaction();
        tx.setApplicationId(applicationId);
        tx.setAmount(props.fixedAmount());
        tx.setState(TxnState.PAID);
        txRepo.save(tx);

        return true;
    }

    @Transactional
    public void cancelApplication(String applicationId) {
        var app = repo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Application not found"));

        if (app.getStatus() == ApplicationStatus.CANCELLED) return;

        boolean hasPaid = txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
        if (hasPaid) {
            throw new ApplicationAlreadyPaidException("Application already paid, cannot cancel");
        }

        boolean hasOutcome = outcomeRepo.existsByApplicationId(applicationId);
        if (hasOutcome) {
            throw new ApplicationAlreadyProcessedException("Application already processed, cannot cancel");
        }

        app.setStatus(ApplicationStatus.CANCELLED);
        repo.save(app);
    }
}
