package kz.don.auth.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kz.don.auth.application.service.AuthService;
import kz.don.auth.web.dto.request.RefreshTokenRequest;
import kz.don.auth.web.dto.request.AuthRequest;
import kz.don.auth.web.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "APIs for user authentication and authorization")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "User login",
            description = "Authenticates user and returns JWT tokens"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response
    ) {
        setAuthCookies(response, authService.login(request));
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Refresh token",
            description = "Refreshes the access token using a refresh token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token successfully refreshed",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid refresh token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(
            summary = "User logout",
            description = "Invalidates the refresh token and logs out the user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully logged out"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid refresh token",
                    content = @Content
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletResponse response
    ) {
        clearAuthCookies(response);
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    private void setAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
        // Access Token Cookie
        String accessTokenCookie = ResponseCookie.from("accessToken", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(true) // Required for SameSite=None
                .sameSite("None") // Cross-site cookies
                .path("/")
                .maxAge(24 * 60 * 60) // 1 day
                .build()
                .toString();
        response.addHeader("Set-Cookie", accessTokenCookie);

        // Refresh Token Cookie
        String refreshTokenCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true) // Required for SameSite=None
                .sameSite("None") // Cross-site cookies
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .build()
                .toString();
        response.addHeader("Set-Cookie", refreshTokenCookie);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        // Create cookies with expiration in the past
        String accessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(24 * 60 * 60) // Immediately expire
                .build()
                .toString();

        String refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // Immediately expire
                .build()
                .toString();

        response.addHeader("Set-Cookie", accessTokenCookie);
        response.addHeader("Set-Cookie", refreshTokenCookie);
    }
}