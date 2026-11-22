package br.edu.upe.sigep.config;

import br.edu.upe.sigep.dto.request.CoordenadorCredenciaisRequestDTO;
import br.edu.upe.sigep.dto.request.EspecialidadeRequestDTO;
import br.edu.upe.sigep.dto.request.PacienteRequestDTO;
import br.edu.upe.sigep.dto.request.ProfissionalRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioAtualizacaoRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class RequestDtoJsonConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.configureMessageConvertersList(conversores -> {
            for (int i = 0; i < conversores.size(); i++) {
                if (conversores.get(i) instanceof JacksonJsonHttpMessageConverter conversor) {
                    conversores.add(i, new CredenciaisJsonConverter(mapeadorEstrito(conversor)));
                    break;
                }
            }
        });
        builder.configureMessageConverters(converter -> {
            if (converter instanceof JacksonJsonHttpMessageConverter jsonConverter
                    && !(jsonConverter instanceof CredenciaisJsonConverter)) {
                JsonMapper strictMapper = mapeadorEstrito(jsonConverter);

                for (Class<?> requestType : new Class<?>[] {
                        EspecialidadeRequestDTO.class,
                        PacienteRequestDTO.class,
                        ProfissionalRequestDTO.class,
                        UsuarioAtualizacaoRequestDTO.class
                }) {
                    jsonConverter.registerMappersForType(requestType, mappings ->
                            jsonConverter.getSupportedMediaTypes().forEach(
                                    mediaType -> mappings.put(mediaType, strictMapper)));
                }
            }
        });
    }

    private static JsonMapper mapeadorEstrito(JacksonJsonHttpMessageConverter conversor) {
        return conversor.getMapper().rebuild()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    private static final class CredenciaisJsonConverter extends JacksonJsonHttpMessageConverter {

        private CredenciaisJsonConverter(JsonMapper mapper) {
            super(mapper);
        }

        @Override
        public boolean canRead(ResolvableType type, MediaType mediaType) {
            Class<?> dto = type.resolve();
            return (dto == UsuarioCadastroRequestDTO.class || dto == CoordenadorCredenciaisRequestDTO.class)
                    && super.canRead(type, mediaType);
        }

        @Override
        public boolean canWrite(ResolvableType type, Class<?> valueClass, MediaType mediaType) {
            return false;
        }

        @Override
        public Object read(ResolvableType type, HttpInputMessage input, Map<String, Object> hints)
                throws IOException {
            try {
                return super.read(type, input, hints);
            } catch (HttpMessageNotReadableException e) {
                // O erro do parser pode conter trechos de uma senha.
                throw new HttpMessageNotReadableException("Corpo JSON inválido.", input);
            }
        }
    }
}
