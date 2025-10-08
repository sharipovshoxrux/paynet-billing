package uz.baraka.paynetbilling.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.baraka.paynetbilling.config.AppProps;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;
import uz.baraka.paynetbilling.domain.ApplicationStatus;
import uz.baraka.paynetbilling.domain.dto.request.CreateApplicationRequest;
import uz.baraka.paynetbilling.domain.dto.response.CreateApplicationResponse;
import uz.baraka.paynetbilling.application.ApplicationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService service;
    private final AppProps props;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateApplicationResponse> create(@RequestBody @Valid CreateApplicationRequest body) {
        var res = service.create(new ApplicationService.CreateRequest(
                body.userId(), body.pinfl(), body.name(), body.source(), body.purpose(), body.bankType()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateApplicationResponse(
                        res.applicationId(), res.name(), res.amount(), res.source(), res.purpose()
                ));
    }

    @GetMapping(value = "/get-price", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getFixedPrice() {
        return ResponseEntity.ok(Map.of("fixedAmount", props.fixedAmount()));
    }

    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApplicationService.ApplicationStatusResponse>> getApplicationsStatus(
            @PathVariable UUID userId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) ApplicationPurpose purpose,
            @RequestParam(required = false) ApplicationSource source
    ) {
        var res = service.findStatusesByUserId(userId, status, purpose, source);
        return ResponseEntity.ok(res);
    }

    /**
     * Internal webhook-like endpoint: another service notifies that a card
     * has been issued/re-issued for applicationId.
     * Returns 204 No Content.
     */
    @PostMapping(
            value = "/outcomes",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> recordOutcome(@RequestBody @Valid ApplicationOutcomeRequest body) {
        service.recordOutcome(body.applicationId(), body.applicationPurpose());
        return ResponseEntity.noContent().build();
    }


    @PostMapping(value = "/{applicationId}/cancel")
    public ResponseEntity<Void> cancelApplication(@PathVariable String applicationId) {
        service.cancelApplication(applicationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/mark-paid",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> markAsPaid(@RequestBody @Valid MarkPaidRequest body) {
        boolean success = service.markPaidByBank(body.applicationId());
        return ResponseEntity.ok(Map.of("success", success));
    }

    public record MarkPaidRequest(@NotBlank String applicationId) {}

    public record ApplicationOutcomeRequest(
            @NotBlank String applicationId,
            @NotNull ApplicationPurpose applicationPurpose
    ) {}
}
