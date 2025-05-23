package com.video.stream.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "yt_course")
public class Course {
    @Id
    private String Id;
    private  String title;
//    @OneToMany
//    private List<Video> list=new ArrayList<>();

}
