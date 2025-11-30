package kr.muroom.muroombackendbach.user.application;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.user.presentation.dto.OwnerDto;
import kr.muroom.muroombackendbach.user.presentation.dto.OwnerDto.OwnerLoginRequest;
import org.springframework.stereotype.Service;

@Service
public class OwnerAuthService {

  public void login(@Valid OwnerDto.OwnerLoginRequest request) {
        
  }
}
