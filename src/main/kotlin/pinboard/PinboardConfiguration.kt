package pinboard

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Configuration
@EnableConfigurationProperties(PinboardProperties::class)
class PinboardConfiguration {

	@Bean
	@ConditionalOnMissingBean
	fun pinboardClient(properties: PinboardProperties, rt: RestTemplate) = PinboardClient(properties.token!!, rt)

	@Bean
	@ConditionalOnMissingBean
	fun restTemplate() = RestTemplate()

}