package kr.muroom.muroombackendbach.user.application;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerLoginRequest;
import org.springframework.stereotype.Service;

@Service
public class OwnerAuthService {

  public void login(@Valid OwnerLoginRequest request) {

  }
}
