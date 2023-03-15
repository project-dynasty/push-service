package com.projectdynasty.push;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import de.alexanderwodarz.code.log.Level;
import de.alexanderwodarz.code.log.Log;
import de.alexanderwodarz.code.web.rest.RequestData;
import de.alexanderwodarz.code.web.rest.authentication.Authentication;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class JwtUtils {

    private final String secret = PushService.CONFIG.get("jwt", PushService.Jwt.class).getKey();

    public boolean validateJwtToken(String authToken) {
        try {
            PushService.VERIFIER.verify(authToken);
            return getClaim(authToken, "refresh").isMissing();
        } catch (SignatureVerificationException | JWTDecodeException |
                 TokenExpiredException e) {
            Log.log(e.getMessage(), Level.ERROR);
        }
        return false;
    }

    public String getSubject(String token) {
        try {
            return PushService.VERIFIER.verify(token).getSubject();
        } catch (TokenExpiredException | SignatureVerificationException e) {
            return null;
        }
    }

    public Claim getClaim(String token, String claim) {
        try {
            return PushService.VERIFIER.verify(token).getClaim(claim);
        } catch (TokenExpiredException e) {
            return null;
        }
    }
}
