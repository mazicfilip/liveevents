package com.mazicfilip.liveevents.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Configuration
@Profile("mock")
public class WireMockDevConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer() {

        WireMockConfiguration opts = WireMockConfiguration.options()
                .port(8081)
                .templatingEnabled(true);

        WireMockServer wm = new WireMockServer(opts);

        wm.stubFor(get(urlPathMatching("/score/([^/]+)"))
                .willReturn(okJson("""
                        {
                          "eventId": "{{request.path.[1]}}",
                          "currentScore": "{{randomInt lower=0 upper=6}}:{{randomInt lower=0 upper=6}}"
                        }
                        """)
                        .withTransformers("response-template")));

        return wm;
    }
}
