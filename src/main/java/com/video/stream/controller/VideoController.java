package com.video.stream.controller;

import com.video.stream.StreamApplication;
import com.video.stream.config.AppConstants;
import com.video.stream.entities.Video;
import com.video.stream.payload.CustomMessage;
import com.video.stream.payload.VideoHomeDTO;
import com.video.stream.repository.VideoRepository;
import com.video.stream.service.impl.VideoServiceimpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {
    @Value("${file.video.hsl}")
    private String HSL_DIR;

    private final VideoRepository videoRepository;

    private final StreamApplication streamApplication;
    @Autowired
    private VideoServiceimpl videoServiceimpl;

    VideoController(StreamApplication streamApplication, VideoRepository videoRepository) {
        this.streamApplication = streamApplication;
        this.videoRepository = videoRepository;
    }

    @PostMapping("")
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
                    @RequestParam(value="poster") MultipartFile poster) {
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        // video.setVideoId(UUID.randomUUID().toString());

        Video saveVideo = videoServiceimpl.save(video, file,poster);
        if (saveVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(saveVideo);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomMessage.builder().message("Video not uplaoded")
                            .success(false)
                            .build());

        }

    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(
            @PathVariable Long videoId) {
        Video video = videoServiceimpl.getVideoById(videoId);
        String contentType = video.getContentType();
        String filePath = video.getFilePath();
        Resource resource = new FileSystemResource(filePath);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    // @GetMapping("")
    // public List<Video> getAllVideos() {
    //     return videoServiceimpl.getAllVideos();

    // }

    // stream videos in chunks
    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(
            @PathVariable Long videoId,
            @RequestHeader(value = "Range", required = false) String range) {
        System.out.println(range);
        Video video = videoServiceimpl.getVideoById(videoId);
        Path path = Paths.get(video.getFilePath());
        String contentType = video.getContentType();
        System.out.println("Content Type: " + contentType);
        Resource resource = new FileSystemResource(path);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        // file length
        long length = path.toFile().length();
        // pahle jaisa code hai kyun ki range header null hai
        if (range == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }
        // calculate the range
        // Range : bytes=0-100
        long rangeStart;
        long rangeEnd;
        // Range header is present
        // Rage : bytes=0-100
        // bytes=1001-1002

        String[] ranges = range.replace("bytes=", "").split("-");
        rangeStart = Long.parseLong(ranges[0]);

        rangeEnd = rangeStart + AppConstants.CHUNK_SIZE - 1;
        if (rangeEnd > length - 1) {
            rangeEnd = length - 1;
        }
        // if (ranges.length > 1) {
        // rangeEnd = Long.parseLong(ranges[1]);
        // } else {
        // rangeEnd = length - 1;
        // }
        // if (rangeEnd > length - 1) {
        // rangeEnd = length - 1;
        // }
        InputStream inputStream = null;
        System.out.println("Range Start: " + rangeStart);
        System.out.println("Range End: " + rangeEnd);
        System.out.println("Length: " + length);
        try {

            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);
            System.out.println("read(number of bytes): " + read);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + length);

            headers.add("Cache-Control", "no-cache");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.add("Accept-Ranges", "bytes");
            headers.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));

        } catch (IOException e) {
            e.printStackTrace();
            // Return error if stream cannot be opened
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        }
        // if (inputStream == null) {
        // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        //

    }

    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serverMasterFile(
            @PathVariable Long videoId) {

        //creating the path
        Path path = Paths.get(HSL_DIR, String.valueOf(videoId), "master.m3u8");
        System.out.println("Path: " + path);
        if (!Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .body(resource);

    }

    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(
            @PathVariable String videoId,
            @PathVariable String segment) {
        Path path = Paths.get(HSL_DIR, videoId, segment + ".ts");
        if (!Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_TYPE,"video/mp2pt"
                  )
                .body(resource);

    
    }


    @GetMapping("")
    public List<VideoHomeDTO> getAllVideos() {
        List<Video> videos = videoServiceimpl.getAllVideos();
        return videos.stream().map(video -> {
            VideoHomeDTO dto = new VideoHomeDTO();
            dto.setVideoId(video.getVideoId());
            dto.setTitle(video.getTitle());
            dto.setDescription(video.getDescription());
            dto.setPosterUrl("/api/v1/videos/" + video.getVideoId() + "/poster");
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{videoId}/poster")
public ResponseEntity<Resource> getPoster(@PathVariable Long videoId) {
    Video video = videoServiceimpl.getVideoById(videoId);
    if (video.getPosterPath() == null) {
        return ResponseEntity.notFound().build();
    }
    Resource resource = new FileSystemResource(video.getPosterPath());
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(video.getPosterContentType()))
            .body(resource);
}
}



