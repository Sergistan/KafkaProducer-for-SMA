package com.utochkin.kafkaproducerforsma.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Builder;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Jwt ответ")
public class JwtResponse implements Serializable {

    @NotNull(message = "id must be not null.")
    @Schema(description = "Id пользователя", example = "1", type = "integer")
    private Long id;

    @NotNull(message = "name must be not null.")
    @Schema(description = "Имя пользователя", example = "Sergey", type = "string")
    private String name;

    @NotNull(message = "accessToken must be not null.")
    @Schema(description = "Токен аутентификации", example = "$2a$12$uhKy.MGqmfYcgbkTd3ZR.eyXKVrU3gm0epHhSy6cfXzydbX4n3Ws2", type = "string")
    private String accessToken;

    @NotNull(message = "refreshToken must be not null.")
    @Schema(description = "Токен обновления токена аутентификации", example = "$2a$12$5lWa/FKZ925t3p.QDvj/oeo38ot7Lna9vhhiCYCOBv8YV0h2uN/5q", type = "string")
    private String refreshToken;

}
