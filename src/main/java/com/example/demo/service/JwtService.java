package com.example.demo.service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

@Service
public class JwtService {
	
	@Value("${jwt.secret}")
	private String secret_key;

	@Value("${jwt.expiration}")
	private long expiration_time;
	private static final long expiration_millis=TimeUnit.HOURS.toMillis(24);
	public String generateToken(String username,String role)
	{
		Date now=new Date();
		Date expiration=new Date(now.getTime()+expiration_millis);
		return Jwts.builder().setSubject(username).setIssuedAt(now).setExpiration(expiration).claim("role",role).signWith(getSignInKey(),SignatureAlgorithm.HS256).compact();
		
	}
	public String extractUsername(String token)
	{
		Claims claims=Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
		return claims.getSubject();
	}
	public String extractRole(String token)
	{
		Claims claims=Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
		return (String) claims.get("role");
	}
	public Date extractExpiration(String token)
	{
		Claims claims=Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
		return claims.getExpiration();
	} 
	public boolean isTokenValid(String token,String username)
	{
		Claims claims=Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
		return claims.getSubject().equals(username) && claims.getExpiration().after(new Date());

	}
	private Key getSignInKey() {
	    byte[] keyBytes = Decoders.BASE64.decode(secret_key);
	    return Keys.hmacShaKeyFor(keyBytes);
	}

}
