package com.utochkin.kafkaproducerforsma.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "posts")
@Schema(description = "Post")
public class Post implements Serializable {

    @Schema(description = "Id поста", example = "1", type = "long")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Описание поста", example = "Description", type = "string")
    @Column(name = "description")
    private String description;

    @Schema(description = "Сообщение поста", example = "Message", type = "string")
    @Column(name = "message")
    private String message;

    @Schema(description = "Ссылка на загруженную картинку", example = "http://localhost:9000/images/325c0226-8acb-41ad-a214-40ff773b35bc%3A%20%3C2024-01-11T16%3A35%3A05.852513900%3E%20image_name.jpeg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20240111%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240111T133505Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=ccdb4e6f980e323932d98dc5ae6ab6ba04925f7fb5fe113afa76b64df71bd92d",
            type = "string",requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @Column(name = "image_link",length = 2024)
    private String imageLink;

    @Schema(description = "Название загруженной картинки", example = "325c0226-8acb-41ad-a214-40ff773b35bc: <2024-01-11T16:35:05.852513900> image_name.jpeg",
            type = "string", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @Column(name = "image_name", length = 2024)
    private String imageName;

    @Schema(description = "Дата и время создания(изменения) поста", example = "2024-01-12 13:56", type = "string", pattern = "yyyy-MM-dd HH:mm",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @Column(name = "created_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    @Schema(description = "Id пользователя поста", example = "1", type = "string", accessMode = Schema.AccessMode.READ_ONLY)
    @ManyToOne
    @JoinColumn (name = "user_id")
    @JsonIgnore
    private User user;
}
