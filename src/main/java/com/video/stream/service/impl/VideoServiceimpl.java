package com.video.stream.service.impl;

import com.video.stream.StreamApplication;
import com.video.stream.entities.Video;
import com.video.stream.repository.VideoRepository;
import com.video.stream.service.VideoService;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoServiceimpl implements VideoService {

    private final StreamApplication streamApplication;
    @Value("${file.vieo}")
    String DIR;
    @Value("${file.poster}")
    String DIR_POSTER;
    @Value("${file.video.hsl}")
    String HSL_DIR;
    private VideoRepository videoRepository;

    public VideoServiceimpl(VideoRepository videoRepository, StreamApplication streamApplication) {
        this.videoRepository = videoRepository;
        this.streamApplication = streamApplication;
    }

    @PostConstruct
    public void init() {

        File file = new File(DIR);
        File file2 = new File(HSL_DIR);
        try {
            Files.createDirectories(Paths.get(HSL_DIR));
            Files.createDirectories(Paths.get(DIR_POSTER));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // if (!file2.exists()) {
        // file2.mkdir();
        // System.out.println("Directory created2");
        // } else {
        // System.out.println("Directory already exists2");
        // }

        if (!file.exists()) {
            file.mkdir();
            System.out.println("Directory created");
        } else {
            System.out.println("Directory already exists");
        }
    }

    @Override
    public Video save(Video video, MultipartFile file, MultipartFile poster) {
        Path videoPath = null;
        Path posterPath = null;
        try {
            // original file name
            String filename = file.getOriginalFilename();
            String postername = poster.getOriginalFilename();
            String contentType = file.getContentType();
            String posterContentType = poster.getContentType();
            System.out.println("file content type"+contentType);
            System.out.println("Poste Content Type"+posterContentType);
            System.out.println("file name"+filename);
            System.out.println("poster name"+postername);
            InputStream inputStream = file.getInputStream();
            String cleanFileName = StringUtils.cleanPath(filename);
            String cleanPosterName = StringUtils.cleanPath(postername);

            String cleanFolder = StringUtils.cleanPath(DIR);
            String cleanPath = StringUtils.cleanPath(DIR_POSTER);
            videoPath= Paths.get(cleanFolder, cleanFileName);
            System.out.println(videoPath);
            Files.copy(inputStream, videoPath, StandardCopyOption.REPLACE_EXISTING);
            // save poster
            InputStream posterInputStream = poster.getInputStream();
            posterPath = Paths.get(cleanPath, cleanPosterName);
            System.out.println(posterPath);
            Files.copy(posterInputStream, posterPath, StandardCopyOption.REPLACE_EXISTING);

            // video meta data
            video.setContentType(contentType);
            video.setFilePath(videoPath.toString());
            video.setPosterPath(posterPath.toString());
            video.setPosterContentType(posterContentType);

            // Save metadata before processing (best practice)
            Video savedVideo = videoRepository.save(video);

            try {
                processVideo(savedVideo.getVideoId());
            } catch (Exception e) {
                // If processing fails, delete the uploaded file and remove metadata
                try {
                    if (videoPath != null && Files.exists(videoPath)) {
                        Files.delete(videoPath);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                videoRepository.deleteById(savedVideo.getVideoId());
                // Comment: Deleted the actual file and metadata due to processing exception
                throw e;
            }
            return savedVideo;
        } catch (Exception e) {
            // Cleanup if file was uploaded but something else failed
            try {
                if (videoPath != null && Files.exists(videoPath)) {
                    Files.delete(videoPath);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Comment: Deleted the actual file due to upload/processing exception
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Video getVideoById(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
        return video;
    }

    @Override
    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    @Override
    public List<Video> getVideoByTitle(String title) {
        return List.of();
    }

    @Override
    public long processVideo(long videoId) {

        Video video = this.getVideoById(videoId);
        String filePath = video.getFilePath();

        // path where to store the data
        Path videoPath = Paths.get(filePath);
        // String outPut360 = HSL_DIR + videoId + "/360p/";
        // String outPut480 = HSL_DIR + videoId + "/480p/";
        // String outPut720 = HSL_DIR + videoId + "/720p/";
        // String outPut1080 = HSL_DIR + videoId + "/1080p/";

        try {
            // Files.createDirectories(Paths.get(outPut360));
            // Files.createDirectories(Paths.get(outPut480));
            // Files.createDirectories(Paths.get(outPut720));
            // Files.createDirectories(Paths.get(outPut1080));
            // ffmpeg command
            Path outputPath = Paths.get(HSL_DIR,String.valueOf(videoId));
            Files.createDirectories(outputPath);

            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath);
            System.out.println(ffmpegCmd);
            System.out.println(ffmpegCmd);
            // file this command
            // ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c",
            // ffmpegCmd);
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("video processing failed!!");
            }

            return videoId;

        } catch (IOException e) {
            throw new RuntimeException("Error creating directories", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // StringBuilder ffmpegCmd=new StringBuilder();
        // ffmpegCmd.append("ffmpeg -i")
        // .append(videoPath.toString())
        // .append("")
        // .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
        // .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
        // .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
        // .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
        // .append("-master_pl_name
        // ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
        // .append("-f hls -hls_time 10 -hls_list_size 0 ")
        // .append("-hls_segment_filename
        // \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
        // .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");

    }
}
