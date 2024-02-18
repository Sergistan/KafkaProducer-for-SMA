package com.utochkin.kafkaproducerforsma.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Jwt запрос")
public class JwtRequest implements Serializable {

    @NotNull(message = "Name must be not null.")
    @Schema(description = "Имя пользователя", example = "Sergey", type = "string")
    private String name;

    @NotNull(message = "Password must be not null.")
    @Schema(description = "Пароль пользователя", example = "111", type = "string")
    private String password;
}
