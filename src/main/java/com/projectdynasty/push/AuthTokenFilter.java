package com.projectdynasty.push;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import de.alexanderwodarz.code.web.rest.RequestData;
import de.alexanderwodarz.code.web.rest.authentication.AuthenticationFilter;
import de.alexanderwodarz.code.web.rest.authentication.AuthenticationFilterResponse;
import de.alexanderwodarz.code.web.rest.authentication.AuthenticationManager;
import de.alexanderwodarz.code.web.rest.authentication.CorsResponse;

public class AuthTokenFilter extends AuthenticationFilter {

    public static AuthenticationFilterResponse doFilter(RequestData request) {
        try {
            String jwt = parseJwt(request.getAuthorization());
            if (jwt != null && PushService.JWT_UTILS.validateJwtToken(jwt)) {
                String username = PushService.JWT_UTILS.getSubject(jwt);
                if (username == null) {
                    if (!request.getPath().startsWith("/send"))
                        return AuthenticationFilterResponse.UNAUTHORIZED();
                } else {
                    if (!(request.getPath().equals("/update") && request.getMethod().equals("POST")))
                        return AuthenticationFilterResponse.UNAUTHORIZED();
                    AccountData account = (AccountData) PushService.DATABASE.getTable(AccountData.class).query().addParameter("username", username).executeOne();
                    if (account == null) {
                        return AuthenticationFilterResponse.UNAUTHORIZED();
                    }
                    AuthenticationManager.setAuthentication(new AccountDataImpl(account));
                }
                return AuthenticationFilterResponse.OK();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthenticationFilterResponse.UNAUTHORIZED();
    }

    public static CorsResponse doCors(RequestData data) {
        CorsResponse response = new CorsResponse();
        response.setCredentials(true);
        response.setOrigin("*");
        response.setHeaders("authorization, content-type, token");
        return response;
    }

    public static String parseJwt(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public String getSubject(String token) {
        try {
            return PushService.VERIFIER.verify(token).getSubject();
        } catch (TokenExpiredException | SignatureVerificationException e) {
            return null;
        }
    }
}