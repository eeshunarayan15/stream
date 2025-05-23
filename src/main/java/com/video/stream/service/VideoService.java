package com.video.stream.service;

import com.video.stream.entities.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    Video save(Video video , MultipartFile file,MultipartFile poster);
    Video getVideoById(Long id);
    List<Video> getAllVideos();

    List<Video> getVideoByTitle(String title);

    //video processing url;
long processVideo(long videoId);
}
