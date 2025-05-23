package com.video.stream;

import com.video.stream.service.impl.VideoServiceimpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StreamApplicationTests {
	@Autowired
VideoServiceimpl videoServiceimpl;
	@Test
	void contextLoads() {
		// videoServiceimpl.processVideo("c76e388d-0171-45c4-848b-4f111a75052d");
	}


}
