package com.projectdynasty.push;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.alexanderwodarz.code.JavaCore;
import de.alexanderwodarz.code.database.Database;
import de.alexanderwodarz.code.model.varible.VaribleMap;
import de.alexanderwodarz.code.web.WebCore;
import de.alexanderwodarz.code.web.rest.annotation.RestApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.HashMap;

@RestApplication
public class PushService {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final JsonConfig CONFIG = new JsonConfig(new File("config.json"));
    public static Database DATABASE;
    public static JWTVerifier VERIFIER;
    public static JwtUtils JWT_UTILS;
    public static HashMap<Boolean, ApnsClient> CLIENTS = new HashMap<>();

    @SneakyThrows
    public static void main(String[] args) {
        initSettings();
        VERIFIER = JWT.require(Algorithm.HMAC256(CONFIG.get("jwt", Jwt.class).getKey())).withIssuer(CONFIG.get("jwt", Jwt.class).getIss()).build();

        DatabaseConfig databaseConfig = CONFIG.get("db", DatabaseConfig.class);
        DATABASE = new Database(databaseConfig.getHost(), databaseConfig.getUsername(), databaseConfig.getPassword(), databaseConfig.getDb());

        boolean live = CONFIG.get("apns", ApnsConfig.class).isLive();

        CLIENTS.put(live, new ApnsClientBuilder()
                .setApnsServer(CONFIG.get("apns", ApnsConfig.class).isLive() ? ApnsClientBuilder.PRODUCTION_APNS_HOST : ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                .setClientCredentials(new File(CONFIG.get("apns", ApnsConfig.class).getPath()), CONFIG.get("apns", ApnsConfig.class).getPassword())
                .build());
        if(live){
            CLIENTS.put(false, new ApnsClientBuilder()
                    .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                    .setClientCredentials(new File(CONFIG.get("apns", ApnsConfig.class).getPath()), CONFIG.get("apns", ApnsConfig.class).getPassword())
                    .build());
        }
        JWT_UTILS = new JwtUtils();
        VaribleMap map = new VaribleMap();
        map.put("port", "7202");
        WebCore.start(PushService.class, map);
    }

    private static void initSettings() {
        if (new File("config.json").exists()) {
            return;
        }
        ApnsConfig apns = new ApnsConfig("./proj.p12", "", "com.projectdynasty.mobileapp", false);
        DatabaseConfig database = new DatabaseConfig("secret", "localhost", "tcp_data", "restapi", 3306);
        Jwt jwt = new Jwt("TCP Rest API", JavaCore.getRandomString(128));

        CONFIG.set("apns", apns);
        CONFIG.set("jwt", jwt);
        CONFIG.set("db", database);
        CONFIG.saveConfig();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Jwt {
        private String iss, key;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DatabaseConfig {
        private String password, host, db, username;
        private int port;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ApnsConfig {
        private String path, password, topic;
        private boolean live;
    }

}
