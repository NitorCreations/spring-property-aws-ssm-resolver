package com.nitorcreations.spring.aws;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import org.springframework.stereotype.Component;

@Component
public class AwsSsmClient {

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
