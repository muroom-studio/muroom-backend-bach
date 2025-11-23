package kr.muroom.muroombackendbach.user.presentation;

import kr.muroom.muroombackendbach.user.application.OwnerService;
import kr.muroom.muroombackendbach.user.presentation.dto.OwnerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @PostMapping
    public ResponseEntity<Void> registerOwner(@RequestBody OwnerDto.OwnerSignUpDto request) {
        ownerService.registerOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }



}
