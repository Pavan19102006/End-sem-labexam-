package com.example.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Encoders;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestExecutionListeners(listeners = org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class, inheritListeners = false)
class JwtApplicationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtService service;

    @Test void generatesAndVerifiesUserClaims() throws Exception {
        String result = mvc.perform(post("/api/jwt/generate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"student\",\"roles\":[\"USER\"],\"email\":\"student@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(result).get("token").asText();
        assertThat(token.split("\\.")).hasSize(3);
        mvc.perform(post("/api/jwt/verify").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.sub").value("student"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.exp").isNumber());
    }
    @Test void rejectsBadPayloadsAndTokens() throws Exception {
        for (String payload : List.of("{}", "{\"username\":\" \"}", "{\"username\":\"student\",\"exp\":1}", "{")) {
            mvc.perform(post("/api/jwt/generate").contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/jwt/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"not-a-token\"}")).andExpect(status().isUnauthorized());
    }
    @Test void rejectsExpiredAndWronglySignedTokens() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService configured = new JwtService(Encoders.BASE64.encode(key.getEncoded()), 3600);
        String expired = Jwts.builder().issuer("jwt-generator").subject("student")
                .expiration(new Date(System.currentTimeMillis() - 60000)).signWith(key).compact();
        assertThatThrownBy(() -> configured.verify(expired)).isInstanceOf(ExpiredJwtException.class);
        String other = new JwtService("", 3600).generate(Map.of("username", "student")).token();
        assertThatThrownBy(() -> configured.verify(other)).isInstanceOf(JwtException.class);
    }
    @Test void swaggerIsAvailable() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/jwt/generate']").exists());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
