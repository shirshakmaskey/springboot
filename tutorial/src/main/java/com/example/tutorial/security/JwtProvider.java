package com.example.tutorial.security;

import com.example.tutorial.exception.SpringRedditException;
import com.example.tutorial.model.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtProvider {

    private KeyStore keyStore;

    @PostConstruct
    public void init(){
        try{
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/springblog.jks");
            keyStore.load(resourceAsStream,"secret".toCharArray());
        }
        catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e){
            throw new SpringRedditException("Exception occurred while loading keystore: "+e);
        }
    }
    public String generateToken(Authentication authentication){
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        return Jwts.builder().setSubject(principal.getUsername()).signWith(getPrivateKey()).compact();
    }
    private PrivateKey getPrivateKey(){
        try{
            return (PrivateKey) keyStore.getKey("springblog","secret".toCharArray());
        }
        catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e){
            throw new SpringRedditException("Exception occurred while while retrieving public key from keystore");
        }
    }
    public Boolean validateToken(String jwt){
        Jwts.parserBuilder().setSigningKey(getPublicKey()).build().parseClaimsJws(jwt);
        return true;
    }

    private PublicKey getPublicKey() {
        try{
            return keyStore.getCertificate("springblog").getPublicKey();
        }
        catch (KeyStoreException e){
            throw new SpringRedditException("Exception occurred while retrieving public key");
        }
    }

    public String getUsernameFromJwt(String token){
        Claims claims = Jwts.parserBuilder().setSigningKey(getPublicKey()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
