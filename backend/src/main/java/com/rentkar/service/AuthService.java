package com.rentkar.service;

import com.rentkar.dto.LoginRequest;
import com.rentkar.dto.LoginResponse;
import com.rentkar.dto.RegisterRequest;
import com.rentkar.dto.UserDTO;

public interface AuthService {
    
    UserDTO register(RegisterRequest request);
    
    LoginResponse login(LoginRequest request);
    
    UserDTO getCurrentUser(String username);
}
