package br.com.desafio.cartoes.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApiController {

    @GetMapping(path = "/openapi.yaml", produces = "application/yaml")
    public Resource openApiYaml() {
        return new ClassPathResource("openapi.yaml");
    }
}
