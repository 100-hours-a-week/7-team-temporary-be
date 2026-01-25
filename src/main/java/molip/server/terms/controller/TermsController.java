package molip.server.terms.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.terms.dto.request.TermsSignRequest;
import molip.server.terms.dto.response.TermsItemResponse;
import molip.server.terms.dto.response.TermsSignItemResponse;
import molip.server.terms.dto.response.TermsSignResponse;
import molip.server.terms.facade.TermsCommandFacade;
import molip.server.terms.service.TermsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TermsController implements TermsApi {
    private final TermsService termsService;
    private final TermsCommandFacade termsCommandFacade;

    @GetMapping("/terms")
    @Override
    public ResponseEntity<ServerResponse<List<TermsItemResponse>>> getTerms() {
        List<TermsItemResponse> content = termsService.getActiveTerms();

        return ResponseEntity.ok(ServerResponse.success(SuccessCode.TERMS_LIST_SUCCESS, content));
    }

    @PostMapping("/terms-sign/{termsId}")
    @Override
    public ResponseEntity<ServerResponse<TermsSignResponse>> createTermsSign(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long termsId,
            @RequestBody TermsSignRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        TermsSignResponse response =
                termsCommandFacade.createTermsSign(userId, termsId, request.isAgreed());

        return ResponseEntity.ok(ServerResponse.success(SuccessCode.TERMS_SIGN_CREATED, response));
    }

    @PatchMapping("/terms-sign/{termsId}")
    @Override
    public ResponseEntity<Void> updateTermsSign(
            @PathVariable Long termsId, @RequestBody TermsSignRequest request) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/terms-sign")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<TermsSignItemResponse>>> getMyTermsSigns() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping("/terms-sign/{termsSignId}")
    @Override
    public ResponseEntity<ServerResponse<TermsSignItemResponse>> getTermsSign(
            @PathVariable Long termsSignId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }
}
