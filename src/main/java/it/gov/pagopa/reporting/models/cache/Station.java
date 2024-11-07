package it.gov.pagopa.reporting.models.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Station {

    @JsonProperty(value = "station_code")
    private String stationCode;

    @JsonProperty(value = "enabled")
    private Boolean enabled;

    @JsonProperty(value = "broker_code")
    private String brokerCode;

    @JsonProperty(value = "password")
    private String password;
}