package com.utochkin.kafkaproducerforsma.services.interfaces;


import com.utochkin.kafkaproducerforsma.dto.request.JwtRequest;
import com.utochkin.kafkaproducerforsma.dto.response.JwtResponse;

public interface AuthService {

    JwtResponse login (JwtRequest jwtRequest);
    JwtResponse refresh(String refreshToken);

}
