package yeonleaf.plantodo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@OpenAPIDefinition(servers = {
		@Server(url = "https://plantodo.site/", description = "Prod Server URL"),
		@Server(url = "http://localhost:8080/", description = "Dev server URL (window)"),
		@Server(url = "http://172.28.47.60:8080/", description = "Dev server URL (ubuntu)")
})
@SpringBootApplication
public class PlantodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlantodoApplication.class, args);
	}

}
