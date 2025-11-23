package kz.don.auth.web.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import kz.don.auth.application.service.AuthService;
import kz.don.auth.domain.entity.User;
import kz.don.auth.domain.repository.UserRepository;
import kz.don.auth.web.dto.request.RegisterRequest;
import kz.don.auth.web.dto.response.AuthResponse;
import kz.don.auth.web.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.hibernate.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "Administrative operations for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all registered users (Admin only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved users list",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            )
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream()
                .map(this::mapToUserResponse)
                .toList());
    }

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account (Admin only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) throws Exception {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "Update user status",
            description = "Enable or disable user account (Admin only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User status successfully updated"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            )
    })
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}