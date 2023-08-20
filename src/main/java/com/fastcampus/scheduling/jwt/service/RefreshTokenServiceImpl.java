package com.fastcampus.scheduling.jwt.service;

import static com.fastcampus.scheduling._core.errors.ErrorMessage.TOKEN_NOT_VALID;
import static com.fastcampus.scheduling._core.errors.ErrorMessage.USER_NOT_FOUND;

import com.fastcampus.scheduling._core.errors.exception.Exception401;
import com.fastcampus.scheduling._core.util.JwtTokenProvider;
import com.fastcampus.scheduling.jwt.dto.RefreshAccessTokenRequestDto;
import com.fastcampus.scheduling.jwt.dto.RefreshAccessTokenResponseDto;
import com.fastcampus.scheduling.jwt.model.RefreshToken;
import com.fastcampus.scheduling.jwt.repository.RefreshTokenRepository;
import com.fastcampus.scheduling.user.model.User;
import com.fastcampus.scheduling.user.repository.UserRepository;
import com.fastcampus.scheduling.user.service.UserService;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserService userService;

	@Transactional
	@Override
	public void updateRefreshToken(String userId, String refreshToken, String newRefreshToken) {
		RefreshToken findRefreshToken = findRefreshToken(userId, refreshToken);
		String newRefreshTokenId = jwtTokenProvider.getRefreshTokenId(newRefreshToken);
		findRefreshToken.updateRefreshTokenId(newRefreshTokenId);
		refreshTokenRepository.save(findRefreshToken).toString();
	}

	@Transactional
	@Override
	public void saveRefreshToken(Long id, String uuid) {
		Optional<RefreshToken> findRefreshTokenOpt = refreshTokenRepository.findByUserId(id);
		if (findRefreshTokenOpt.isEmpty()) {
			refreshTokenRepository.save(RefreshToken.of(id, uuid));
			return;
		}
		RefreshToken findRefreshToken = findRefreshTokenOpt.get();
		findRefreshToken.updateRefreshTokenId(uuid);
		refreshTokenRepository.save(findRefreshToken);
	}

	@Override
	public RefreshAccessTokenResponseDto refreshAccessToken(String userId) {
		try {
			User findUser = userRepository.findById(Long.valueOf(userId))
					.orElseThrow(() -> new Exception401(
							"User Id : " + userId + " " + USER_NOT_FOUND));

			Authentication authentication = getAuthentication(findUser.getUserEmail());
			Collection<GrantedAuthority> roles = (Collection<GrantedAuthority>) authentication.getAuthorities();

			String newAccessToken = jwtTokenProvider.generateJwtAccessToken(userId, "/refresh-token",
					roles);

			return RefreshAccessTokenResponseDto.builder()
					.accessToken(newAccessToken)
					.build();
		} catch (Exception exception) {
			throw new Exception401(TOKEN_NOT_VALID);
		}

	}

	@Transactional
	@Override
	public void revokeToken(String refreshToken) {
		String userId = jwtTokenProvider.getUserId(refreshToken);
		refreshTokenRepository.deleteByUserId(Long.valueOf(userId));
	}

	public Authentication getAuthentication(String email) {
		UserDetails userDetails = userService.loadUserByUsername(email);
		return new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());
	}

	public RefreshToken findRefreshToken(String userId, String refreshToken) {
		RefreshToken findRefreshToken = refreshTokenRepository.findByUserId(Long.valueOf(userId))
				.orElseThrow(
						() -> new Exception401(TOKEN_NOT_VALID));

		String findRefreshTokenId = findRefreshToken.getRefreshTokenId();
		if (!jwtTokenProvider.equalRefreshTokenId(findRefreshTokenId, refreshToken)) {
			throw new Exception401(TOKEN_NOT_VALID);
		}
		return findRefreshToken;
	}

	@Override
	public String getRefreshToken(RefreshAccessTokenRequestDto request) {

		String refreshToken = request.getRefreshToken();

		jwtTokenProvider.validateJwtToken(refreshToken);

		return refreshToken;
	}
}
