package com.checkmate.bub;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.checkmate.bub.ai.clova.ClovaClient;
import com.checkmate.bub.ai.clova.ClovaSpeechClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "common"})
class BubApplicationTests {

	// Mock external services to avoid connection issues
	@MockitoBean
	private ClovaClient clovaClient;
	
	@MockitoBean
	private ClovaSpeechClient clovaSpeechClient;

	// @Test
	// void contextLoads() {
	// }

}
