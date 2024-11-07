package it.gov.pagopa.reporting.models.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CreditorInstitutionStation {

    @JsonProperty(value = "creditor_institution_code")
    private String creditorInstitutionCode;

    @JsonProperty(value = "station_code")
    private String stationCode;
}
