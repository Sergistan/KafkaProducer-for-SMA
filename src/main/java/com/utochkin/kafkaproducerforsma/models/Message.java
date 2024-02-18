package com.utochkin.kafkaproducerforsma.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "messages")
public class Message implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false , updatable = false)
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "sent_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sentAt;

    @ManyToOne
    @JoinColumn (name = "sender_id")
    @JsonIgnore
    private User sender;

    @ManyToOne
    @JoinColumn (name = "chat_id")
    @JsonIgnore
    private Chat chat;

}
