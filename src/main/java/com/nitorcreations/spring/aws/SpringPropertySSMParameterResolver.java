package com.nitorcreations.spring.aws;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class SpringPropertySSMParameterResolver implements EnvironmentPostProcessor {

    private static final String PREFIX = "{ssmParameter}";

    private AwsSsmClient aws = new AwsSsmClient();

    /**
     * Processor that iterates through all property sources of the Spring environment to check if there are
     * any properties that match the configured prefix. In case any are found, a new property source is created
     * to override those specific parameters with the correctly resolved ones.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication springApplication) {
        StreamSupport.stream(environment.getPropertySources().spliterator(), false)
            .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
            .forEach(propertySource -> {
                EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
                Map<String, Object> propertyOverrides = Arrays.stream(enumerablePropertySource.getPropertyNames())
                        .filter(p -> (enumerablePropertySource.getProperty(p) instanceof String))
                        .filter(p -> ((String) enumerablePropertySource.getProperty(p)).startsWith(PREFIX))
                        .collect(Collectors.toMap(Function.identity(), p -> aws.resolveSsmParameter((String) enumerablePropertySource.getProperty(p), PREFIX)));

                if (!propertyOverrides.isEmpty()) {
                    PropertySource<?> processedProperties = new MapPropertySource("override-"+ propertySource.getName(), propertyOverrides);
                    environment.getPropertySources().addBefore(propertySource.getName(), processedProperties);
                }
            });
    }

    class AwsSsmClient {

        private AWSSimpleSystemsManagement awsSsm = null;

        String resolveSsmParameter(String value, String prefix) {
            String ssmParameterName = value.substring(prefix.length());
            GetParameterRequest request = new GetParameterRequest().withName(ssmParameterName).withWithDecryption(true);

            return getAWSClient().getParameter(request).getParameter().getValue();
        }

        /**
         * Lazily initiated AWS client to prevent errors when the configuration has no ssm configured parameters
         * and no working AWS environment setup
         *
         * @return AWS SSM client
         */
        private AWSSimpleSystemsManagement getAWSClient() {
            if (awsSsm == null) {
                awsSsm = AWSSimpleSystemsManagementClientBuilder.defaultClient();
            }
            return awsSsm;
        }

    }

}

