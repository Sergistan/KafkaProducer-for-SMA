package com.utochkin.kafkaproducerforsma.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "UserDto request/response")
public class UserDto implements Serializable {

    @Schema(description = "Id пользователя", example = "1", type = "integer", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(description = "Имя пользователя", example = "Sergey", type = "string")
    @NotNull(message = "Name must be not null.")
    @Length(max = 255, message = "Name length must be smaller than 255 symbols.")
    private String name;

    @Schema(description = "Пароль пользователя", example = "111", type = "string")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull(message = "Password must be not null.")
    private String password;

    @NotNull(message = "Email must be not null.")
    @Length(max = 255, message = "Email length must be smaller than 255 symbols.")
    @Schema(description = "Электронная почта пользователя", example = "Sergey@gmail.com", type = "string")
    @Email
    private String email;


}
