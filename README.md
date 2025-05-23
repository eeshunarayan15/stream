# Video Streaming API

This project is a Spring Boot-based backend for a YouTube-like video streaming platform. It supports video upload, HLS streaming, poster image upload, and video metadata management.

---

## Features

- Upload videos with poster images
- Stream videos using HLS (HTTP Live Streaming)
- Serve video poster images
- List all videos with metadata and poster URL
- Chunked video streaming for efficient playback

---

## API Endpoints

### 1. Upload Video

- **URL:** `POST /api/v1/videos`
- **Description:** Upload a video file with title, description, and poster image.
- **Request:**
  - `file` (MultipartFile, required): The video file
  - `title` (String, required): Video title
  - `description` (String, required): Video description
  - `poster` (MultipartFile, required): Poster image file
- **Response:** Video metadata (JSON)

### 2. List All Videos

- **URL:** `GET /api/v1/videos`
- **Description:** Get a list of all videos with title, description, and poster URL.
- **Response:** Array of objects:
  - `videoId`: Video ID
  - `title`: Video title
  - `description`: Video description
  - `posterUrl`: URL to fetch the poster image

### 3. Get Video Poster

- **URL:** `GET /api/v1/videos/{videoId}/poster`
- **Description:** Get the poster image for a video.
- **Response:** Image file (Content-Type: image/png, image/jpeg, etc.)

### 4. Stream Video (Full File)

- **URL:** `GET /api/v1/videos/stream/{videoId}`
- **Description:** Download or stream the full video file.
- **Response:** Video file (Content-Type: video/mp4, etc.)

### 5. Stream Video in Chunks (Range Requests)

- **URL:** `GET /api/v1/videos/stream/range/{videoId}`
- **Description:** Stream video in chunks using HTTP Range headers for efficient playback.
- **Headers:**
  - `Range: bytes=start-end` (optional)
- **Response:** Partial video content (Content-Type: video/mp4, etc.)

### 6. HLS Master Playlist

- **URL:** `GET /api/v1/videos/{videoId}/master.m3u8`
- **Description:** Get the HLS master playlist for a video (for adaptive streaming).
- **Response:** HLS playlist file (Content-Type: application/vnd.apple.mpegurl)

### 7. HLS Video Segments

- **URL:** `GET /api/v1/videos/{videoId}/{segment}.ts`
- **Description:** Get a specific HLS video segment for playback.
- **Response:** TS segment file (Content-Type: video/mp2t)

---

## Database Connection

This project uses a relational database (e.g., MySQL) via Spring Data JPA. Configure your database connection in `src/main/resources/application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/your_db_name
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

- Replace `your_db_name`, `your_db_user`, and `your_db_password` with your actual database credentials.
- Make sure your database server is running and accessible.

---

## Running the Application

1. Build the project:
   ```
   ./mvnw clean install
   ```
2. Run the Spring Boot application:
   ```
   ./mvnw spring-boot:run
   ```
3. The API will be available at `http://localhost:8080/api/v1/videos`

---

## Example Usage

- Upload a video using Postman or a frontend form.
- List all videos: [http://localhost:8080/api/v1/videos](http://localhost:8080/api/v1/videos)
- Get a poster: [http://localhost:8080/api/v1/videos/1/poster](http://localhost:8080/api/v1/videos/1/poster)
- Stream a video: [http://localhost:8080/api/v1/videos/stream/1](http://localhost:8080/api/v1/videos/stream/1)
- HLS playlist: [http://localhost:8080/api/v1/videos/1/master.m3u8](http://localhost:8080/api/v1/videos/1/master.m3u8)

---

## Notes

- Make sure to set up the required directories for video and poster storage as configured in your `application.properties`.
- The API supports CORS for frontend integration.
- For production, secure your endpoints and validate file uploads.
