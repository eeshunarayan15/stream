package com.video.stream.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoHomeDTO {
    private Long videoId;
    private String title;
    private String description;
    private String posterUrl;

}
