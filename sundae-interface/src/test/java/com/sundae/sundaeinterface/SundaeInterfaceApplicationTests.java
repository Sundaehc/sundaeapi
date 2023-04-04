package com.sundae.sundaeinterface;

import com.sundae.sundaeclientsdk.client.SundaeApiClient;
import com.sundae.sundaeclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SundaeInterfaceApplicationTests {

	@Resource
	private SundaeApiClient sundaeApiClient;

	@Test
	void contextLoads() {
		String result = sundaeApiClient.getNameByGet("sundae");
		User user = new User();
		user.setUsername("ouyang");
		String result2 = sundaeApiClient.getUserNameByPost(user);
		System.out.println(result);
		System.out.println(result2);
	}

}
