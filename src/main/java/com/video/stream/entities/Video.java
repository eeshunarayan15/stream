package com.video.stream.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "yt_video")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long videoId;
    private String title;
    private String description;
    private String contentType;
    private String filePath;
    private String posterPath;
    private String posterContentType;

    // @ManyToOne
    // private Course course;
}
