package mk.finki.orderservice.config

import feign.Logger
import feign.Request
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class FeignConfig {

    @Bean
    fun requestOptions(): Request.Options =
        // connectTimeout, readTimeout — kept short since this call blocks order creation
        Request.Options(2000, TimeUnit.MILLISECONDS, 3000, TimeUnit.MILLISECONDS, true)

    @Bean
    fun feignLoggerLevel(): Logger.Level = Logger.Level.BASIC
}
