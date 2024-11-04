package it.gov.pagopa.reporting.models.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CacheResponse {

    @JsonProperty(value = "stations")
    private List<Station> stations;

    @JsonProperty(value = "creditorInstitutionStations")
    private List<CreditorInstitutionStation> creditorInstitutionStations;

    private LocalDate retrieveDate;

}