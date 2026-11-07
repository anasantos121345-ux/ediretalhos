package br.com.edhi.painel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Libera a API para o site e o painel HTML, que podem ser abertos de qualquer origem
 * (inclusive direto do arquivo, file://), e liga a checagem de login.
 * Como o login vai no cabeçalho Authorization (e não em cookie), liberar a origem não expõe a sessão.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AutenticacaoInterceptor autenticacao;

    public CorsConfig(AutenticacaoInterceptor autenticacao) {
        this.autenticacao = autenticacao;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PATCH", "DELETE")
                .allowedHeaders("Content-Type", "Authorization");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacao).addPathPatterns("/api/**");
    }
}
