package com.utochkin.kafkaproducerforsma.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "PostDto request/response")
public class PostDto implements Serializable {

    @Schema(description = "Описание поста", example = "Description", type = "string")
    @NotNull(message = "Description must be not null.")
    @Length(max = 255,
            message = "Description length must be smaller than 255 symbols.")
    private String description;

    @Schema(description = "Сообщение поста", example = "Message", type = "string")
    @NotNull(message = "Message must be not null.")
    @Length(max = 255,
            message = "Message length must be smaller than 255 symbols.")
    private String message;

    @Schema(description = "Ссылка на загруженную картинку", example = "http://localhost:9000/images/325c0226-8acb-41ad-a214-40ff773b35bc%3A%20%3C2024-01-11T16%3A35%3A05.852513900%3E%20image_name.jpeg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20240111%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240111T133505Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=ccdb4e6f980e323932d98dc5ae6ab6ba04925f7fb5fe113afa76b64df71bd92d",
            type = "string",requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @Length(max = 512,
            message = "ImageLink length must be smaller than 512 symbols.")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageLink;

    @Schema(description = "Название загруженной картинки", example = "325c0226-8acb-41ad-a214-40ff773b35bc: <2024-01-11T16:35:05.852513900> image_name.jpeg",
            type = "string", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @Length(max = 255,
            message = "ImageName length must be smaller than 255 symbols.")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageName;

    @Schema(description = "Дата и время создания(изменения) поста", example = "2024-01-12 13:56", type = "string", pattern = "yyyy-MM-dd HH:mm",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

}
