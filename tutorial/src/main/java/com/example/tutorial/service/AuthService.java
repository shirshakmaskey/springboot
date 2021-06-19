package com.example.tutorial.service;

import com.example.tutorial.dto.AuthenticationResponse;
import com.example.tutorial.dto.LoginRequest;
import com.example.tutorial.dto.RegisterRequest;
import com.example.tutorial.exception.SpringRedditException;
import com.example.tutorial.model.NotificationEmail;
import com.example.tutorial.model.User;
import com.example.tutorial.model.VerificationToken;
import com.example.tutorial.repository.UserRepository;
import com.example.tutorial.repository.VerificationRepository;
import com.example.tutorial.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public void signup(RegisterRequest registerRequest){
        User user =  new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepository.save(user);
        String token = generateVerificationToken(user);
        String subject = "Please Activate your Account";
        String message = "Thank you for signing for spring reddit. Please click the below url to activate your account";
        String activationUrl = "http://localhost:8080/api/auth/accountVerification/"+token;
        mailService.sendMail(new NotificationEmail(subject,user.getEmail(),message+activationUrl));
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token){
        Optional<VerificationToken> verificationToken = verificationRepository.findByToken(token);
        verificationToken.orElseThrow(()->new SpringRedditException("Invalid Token"));
        fetchUserAndEnable(verificationToken.get());
    }

    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(()->new SpringRedditException("User not found "+username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);
        return new AuthenticationResponse(token,loginRequest.getUsername());
    }

    public User getCurrentUser() {
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + principal.getUsername()));
    }
}
