package kz.don.auth.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kz.don.auth.domain.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private UUID userId;

    private String username;

    private String fullName;

    private RoleEnum role;
}