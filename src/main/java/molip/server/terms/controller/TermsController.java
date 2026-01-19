package molip.server.terms.controller;

import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.terms.dto.request.TermsSignRequest;
import molip.server.terms.dto.response.TermsItemResponse;
import molip.server.terms.dto.response.TermsSignItemResponse;
import molip.server.terms.dto.response.TermsSignResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TermsController implements TermsApi {
  @GetMapping("/terms")
  @Override
  public ResponseEntity<ServerResponse<PageResponse<TermsItemResponse>>> getTerms() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PostMapping("/terms-sign/{termsId}")
  @Override
  public ResponseEntity<ServerResponse<TermsSignResponse>> createTermsSign(
      @PathVariable Long termsId, @RequestBody TermsSignRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
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
