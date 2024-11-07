package it.gov.pagopa.reporting.functions.concurrent;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import it.gov.pagopa.reporting.client.ApiConfigClient;
import it.gov.pagopa.reporting.exception.Cache4XXException;
import it.gov.pagopa.reporting.exception.Cache5XXException;
import it.gov.pagopa.reporting.functions.RetrieveDetails;
import it.gov.pagopa.reporting.models.cache.CacheResponse;
import it.gov.pagopa.reporting.models.cache.CreditorInstitutionStation;
import it.gov.pagopa.reporting.models.cache.Station;
import it.gov.pagopa.reporting.service.FlowsService;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
@TestInstance(Lifecycle.PER_CLASS)
class RetrieveDetailsConcurrentTest {
	
	@Spy
    RetrieveDetails function;
	
	@Mock
    ApiConfigClient cacheClient;
	
	CacheResponse cacheResponse;
	
	@BeforeEach
	void setup() throws IllegalArgumentException, IOException, Cache5XXException, Cache4XXException {
		RetrieveDetails.setCacheContent(null);
		Station station = Station.builder().stationCode("mockStationCode").brokerCode("mockBrokerCode").password("mockPwd").enabled(true).build();
        CreditorInstitutionStation creditorInstitutionStation = CreditorInstitutionStation.builder().creditorInstitutionCode("00595780131").stationCode("mockStationCode").build();
        cacheResponse = CacheResponse.builder()
        		.creditorInstitutionStations(Collections.singletonList(creditorInstitutionStation))
        		.stations(Collections.singletonList(station))
        		.build();
        lenient().when(cacheClient.getCache()).thenReturn(cacheResponse);
	}
	
	@ParameterizedTest
	@ValueSource(ints = {1, 2})
	void concurrentFlowsServiceInstanceCacheAccessTest(int number) throws Exception {
		Logger logger = Logger.getLogger("RetrieveDetailsConcurrentTest");
		lenient().when(function.getVars(anyString())).thenReturn("60");
		lenient().when(function.getCacheClientInstance()).thenReturn(cacheClient);
		logger.fine("concurrentFlowsServiceInstanceCacheAccess - thread("+number+") start => " + Thread.currentThread().getName());
		FlowsService flowService = function.getFlowsServiceInstance(logger, "00595780131");
		assertNotNull(flowService);
		logger.fine("concurrentFlowsServiceInstanceCacheAccess - thread("+number+") end => " + Thread.currentThread().getName());
		
	}
	
	@AfterAll
	void checkCacheAccessTest() throws IllegalArgumentException, IOException, Cache5XXException, Cache4XXException {
		// check that the cache has only been accessed once
		verify(cacheClient, times(1)).getCache();
	}

}
