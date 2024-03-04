package com.utochkin.kafkaproducerforsma.services.impl;


import com.utochkin.kafkaproducerforsma.dto.request.JwtRequest;
import com.utochkin.kafkaproducerforsma.dto.response.JwtResponse;
import com.utochkin.kafkaproducerforsma.exceptions.BadCredentialsException;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.security.JwtTokenProvider;
import com.utochkin.kafkaproducerforsma.services.interfaces.AuthService;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public JwtResponse login(JwtRequest loginRequest) {
        JwtResponse jwtResponse = new JwtResponse();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getName(), loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException();
        }
        User user = userService.findByName(loginRequest.getName());
        jwtResponse.setId(user.getId());
        jwtResponse.setName(user.getName());
        jwtResponse.setAccessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getName(), user.getRole()));
        jwtResponse.setRefreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getName()));
        return jwtResponse;
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        return jwtTokenProvider.refreshUserTokens(refreshToken);
    }
}
