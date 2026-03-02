package zerotrust.cloud.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@Configuration
public class KubernetesConfig {

    @Value("${kubernetes.config.master-url:}")
    private String masterUrl;

    @Value("${kubernetes.config.token:}")
    private String token;

    @Value("${kubernetes.config.namespace:default}")
    private String namespace;

    @Bean
    public KubernetesClient kubernetesClient() {
        ConfigBuilder configBuilder = new ConfigBuilder();

        if (StringUtils.hasText(masterUrl)) {
            configBuilder.withMasterUrl(masterUrl);
        }
        if (StringUtils.hasText(token)) {
            configBuilder.withOauthToken(token);
        }
        configBuilder.withNamespace(namespace);

        Config config = configBuilder.build();
        return new DefaultKubernetesClient(config);
    }
}
