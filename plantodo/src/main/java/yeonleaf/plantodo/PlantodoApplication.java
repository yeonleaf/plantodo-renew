package yeonleaf.plantodo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(servers = {@Server(url = "https://plantodo.site/", description = "Default Server URL")})
@SpringBootApplication
public class PlantodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlantodoApplication.class, args);
	}

}
