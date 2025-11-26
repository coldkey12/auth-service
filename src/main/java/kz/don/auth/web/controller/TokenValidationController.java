package kz.don.auth.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import kz.don.auth.domain.entity.User;
import kz.don.auth.domain.repository.UserRepository;
import kz.don.auth.infrastructure.security.jwt.JwtService;
import kz.don.auth.web.dto.response.UserValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication Controller", description = "APIs for user authentication and authorization")
public class TokenValidationController {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Validate JWT Token",
            description = "Validates JWT token and returns user information for microservice authentication"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token is valid",
                    content = @Content(schema = @Schema(implementation = UserValidationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired token",
                    content = @Content
            )
    })
    @PostMapping("/validate-token")
    public ResponseEntity<UserValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            log.debug("Validating token from Authorization header");

            // Remove "Bearer " prefix
            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format");
                return ResponseEntity.status(401).build();
            }

            String token = authHeader.substring(7);

            // Extract username from token
            String username = jwtService.extractUsername(token);
            log.debug("Extracted username from token: {}", username);

            // Find user
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        log.warn("User not found: {}", username);
                        return new RuntimeException("User not found");
                    });

            // Validate token
            if (!jwtService.isTokenValid(token, user)) {
                log.warn("Token validation failed for user: {}", username);
                return ResponseEntity.status(401).build();
            }

            // Return user info
            UserValidationResponse response = UserValidationResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .enabled(user.isEnabled())
                    .build();

            log.info("Token validated successfully for user: {}", username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
}