package com.utochkin.kafkaproducerforsma.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chats")
@Schema(description = "Chat request")
public class Chat implements Serializable {

    @Schema(description = "Id чата", example = "1", type = "integer", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Дата и время создания чата", example = "2024-01-12 13:56", type = "String", format = "date-time", pattern = "yyyy-MM-dd HH:mm",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Column(name = "created_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    @Schema(description = "Последенее сообщение в чате", example = "Последенее сообщение в чате", type = "String", accessMode = Schema.AccessMode.READ_ONLY)
    @Column(name = "last_message")
    private String lastMessage;

    @ManyToMany
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(
            name = "users_chats",
            joinColumns = @JoinColumn(name = "chats_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    @OneToMany(cascade = CascadeType.ALL , mappedBy = "chat")
    private List<Message> messages;


}
