# A Simple Pinboard Client

This is a Kotlin-language based Pinboard API client. It works with the version of the API as of 17 August 2017. 

Though it is implemented in Kotlin, it may be used with any JVM-based programming language. Here are some examples of how to instantiate with Java. 

At minimum, the `PinboardClient` requires a valid Pinboad authentication token. You can get this token from the Pinboard website under your profile information. I recommend you keep it outside of the code, in an environment variable, or in the [Spring Cloud Config Server](https://cloud.spring.io/spring-cloud-config/spring-cloud-config.html).  

```java
String pinboardToken = System.getenv("PINBOARD_TOKEN");
PinboardClient pbc = new PinboardClient( pinboardToken );
````

Optionally, you can instantiate it with a particular `RestTemplate` instance that you would like to use. The `PinboardClient` customizes the configured `RestTemplate`, though, reconfiguring the `HttpMessageConverter` instances. 


```java
String pinboardToken = System.getenv("PINBOARD_TOKEN");
RestTemplate rt = new RestTemplate();
PinboardClient pbc = new PinboardClient( pinboardToken, rt );
````
