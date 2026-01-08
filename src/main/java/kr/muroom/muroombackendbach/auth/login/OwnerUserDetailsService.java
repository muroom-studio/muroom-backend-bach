package kr.muroom.muroombackendbach.auth.login;

import java.util.List;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.domain.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerUserDetailsService implements UserDetailsService {

  private final OwnerRepository ownerRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Owner owner = ownerRepository.findByEmail(email)
        .filter(Owner::isActive)
        .orElseThrow(() -> new UsernameNotFoundException("사장님 계정을 찾을 수 없습니다."));

    return new OwnerPrincipal(
        owner.getId(),
        owner.getEmail(),
        owner.getPassword(),
        List.of(new SimpleGrantedAuthority("ROLE_OWNER")),
        true
    );
  }
}
