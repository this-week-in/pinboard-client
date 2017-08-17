package pinboard

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@ConfigurationProperties(prefix = "pinboard")
class PinboardProperties(@NotNull var token: String?) {

    constructor() : this(null)
}