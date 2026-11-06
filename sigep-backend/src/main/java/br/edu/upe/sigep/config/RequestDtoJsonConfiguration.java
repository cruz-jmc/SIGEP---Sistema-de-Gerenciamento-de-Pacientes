package br.edu.upe.sigep.config;

import br.edu.upe.sigep.dto.request.EspecialidadeRequestDTO;
import br.edu.upe.sigep.dto.request.PacienteRequestDTO;
import br.edu.upe.sigep.dto.request.ProfissionalRequestDTO;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class RequestDtoJsonConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.configureMessageConverters(converter -> {
            if (converter instanceof JacksonJsonHttpMessageConverter jsonConverter) {
                JsonMapper strictMapper = jsonConverter.getMapper().rebuild()
                        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .build();

                for (Class<?> requestType : new Class<?>[] {
                        EspecialidadeRequestDTO.class,
                        PacienteRequestDTO.class,
                        ProfissionalRequestDTO.class
                }) {
                    jsonConverter.registerMappersForType(requestType, mappings ->
                            jsonConverter.getSupportedMediaTypes().forEach(
                                    mediaType -> mappings.put(mediaType, strictMapper)));
                }
            }
        });
    }
}
