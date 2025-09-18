package uz.baraka.paynetbilling.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.baraka.paynetbilling.config.AppProps;
import uz.baraka.paynetbilling.domain.dto.request.CreateApplicationRequest;
import uz.baraka.paynetbilling.domain.dto.response.CreateApplicationResponse;
import uz.baraka.paynetbilling.application.ApplicationService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService service;
    private final AppProps props;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateApplicationResponse> create(@RequestBody @Valid CreateApplicationRequest body) {
        var res = service.create(new ApplicationService.CreateRequest(
                body.userId(), body.pinfl(), body.name(), body.source(), body.purpose()
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
}
