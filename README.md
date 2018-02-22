# Spring Property AWS SSM Resolver

## Introduction

[AWS SSM Parameter Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-paramstore.html) enables easy storage of encrypted parameters, such as database passwords or API keys, and easy retrieval during runtime for applications running on AWS infrastructure.

```spring-property-aws-ssm-resolver``` is a small Spring Boot plugin for resolving AWS SSM Parameters during startup simply by using prefixed regular Spring Boot properties.

## Installation

### Maven

    <dependency>
        <groupId>com.nitorcreations</groupId>
        <artifactId>spring-property-aws-ssm-resolver</artifactId>
        <version>1.0.0</version>
    </dependency>

## Usage

Put this plugin on your Spring Boot application classpath.

Make sure your application can find the correct AWS region through the [AWS Default Region Provider Chain](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/java-dg-region-selection.html#default-region-provider-chain). The simplest universal way is through the ```AWS_REGION``` environment variable.

Set up your Spring Properties with the ```{ssmParameter}``` prefix.

Example ```application.yml```:

    my.regular.property: 'Foo'
    my.secret.property: '{ssmParameter}/myproject/myapp/mysecret'

During startup, the plugin would look for properties with this prefix and replace the value by looking for a property called ```/myproject/myapp/mysecret``` on AWS SSM.

The AWS client initialization is lazy, so using this plugin does not require AWS access, provided that the environment contains no properties with this prefix.

The AWS client currently assumes all resolved properties are encrypted on SSM.

Technically the substitution is accomplished by constructing additional Spring property sources with intentionally conflicting property names, and adding them before the property sources in which the prefixed properties were found in.
