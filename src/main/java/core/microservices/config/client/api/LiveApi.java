package core.microservices.config.client.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import core.microservices.config.client.ProjectConfig;
import core.microservices.config.client.dto.ProjectConfigDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class LiveApi {

	@Autowired
	private Environment environment;

	@Autowired
	private ProjectConfig projectConfig;
	
	@GetMapping("/live-check")
	public String liveCheck(@RequestParam(required = false) Integer sleep,
			@RequestHeader("my-app-correlation-id") String correlationId) throws Exception {
		log.info("liveCheck:: " + sleep + " - correlationId:: " + correlationId);
		if(sleep != null) {
			Thread.sleep(sleep*1000);
		}
		Integer port = Integer.parseInt(environment.getProperty("server.port"));
		return String.format("Config Client Server:: %s", port);
	}

	@GetMapping("/env-check")
	public Map<String, String> testParam(@RequestParam("envs") List<String> envs) throws Exception {
		Map<String, String> ret = envs.stream()
				.collect(Collectors.toMap(
						k -> k, 
						v -> environment.getProperty(v)));
		return ret;
	}
	
	@GetMapping("/configuration")
	public ProjectConfigDto configuration() throws Exception {
		return new ProjectConfigDto(projectConfig);
	}
	
	public static void main(String[] args) throws InterruptedException {
		for(int i=1; i<=5; i++) {
			makeHelloCall(i+"", 3);
		}
		Thread.sleep(2000000);
	}
	
	private static void makeHelloCall(String name, Integer sleep) {
		new Thread(() -> {
			int callCount = 0;
			while(true) {
				callCount++;
				String response = new RestTemplate()
						.getForObject("http://localhost:8762/hello?sleep="+sleep+"&name="+name, String.class);
				System.out.println("response:: " + name + " - " + callCount + " - " + response);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
