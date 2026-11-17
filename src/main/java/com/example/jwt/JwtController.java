package com.example.jwt;

import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/jwt")
@Tag(name = "JWT", description = "Lab utility: generate a JWT from an already authenticated user's JSON")
public class JwtController {
    private final JwtService service;
    public JwtController(JwtService service) { this.service = service; }

    @PostMapping("/generate")
    @Operation(summary = "Generate a signed HS256 JWT", description = "Accepts trusted user JSON. username is required. Registered JWT claims are managed by the server. This lab endpoint does not authenticate users.")
    public JwtService.TokenResponse generate(@RequestBody
            @Schema(type = "object", example = "{\"username\":\"harshitha\",\"email\":\"harshitha@example.com\",\"roles\":[\"USER\"]}") Map<String, Object> user) {
        return service.generate(user);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify signature and expiration, then return claims")
    public Map<String, Object> verify(@Valid @RequestBody VerifyRequest request) {
        return service.verify(request.token());
    }
    public record VerifyRequest(@NotBlank String token) {}
}
