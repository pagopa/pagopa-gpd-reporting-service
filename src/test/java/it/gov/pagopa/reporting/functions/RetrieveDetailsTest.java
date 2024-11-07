package it.gov.pagopa.reporting.functions;

import com.microsoft.azure.functions.ExecutionContext;

import it.gov.pagopa.reporting.client.ApiConfigClient;
import it.gov.pagopa.reporting.exception.AppException;
import it.gov.pagopa.reporting.exception.Cache4XXException;
import it.gov.pagopa.reporting.exception.Cache5XXException;
import it.gov.pagopa.reporting.models.cache.CacheResponse;
import it.gov.pagopa.reporting.models.cache.CreditorInstitutionStation;
import it.gov.pagopa.reporting.models.cache.Station;
import it.gov.pagopa.reporting.service.FlowsService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetrieveDetailsTest {

    @Mock
    ExecutionContext context;

    @Spy
    RetrieveDetails function;

    @Mock
    FlowsService flowService;
    
    @Mock
    ApiConfigClient cacheClient;

    @Test
    void runOkTest() throws AppException {

        Logger logger = Logger.getLogger("InfoLogging");

        String message = "{\"idPA\":\"00595780131\",\"flows\":[{\"identificativoFlusso\":\"2021-07-26AGID_02-S000000001\",\"dataOraFlusso\":1627293600000}], \"retry\": 0}";
        when(context.getLogger()).thenReturn(logger);

        doReturn(flowService).when(function).getFlowsServiceInstance(logger, "00595780131");

        function.run(message, context);

        verify(context, times(1)).getLogger();
        verify(flowService, times(1)).flowsXmlDownloading(any(), anyString(), anyInt());
    }
    
    @Test
    void runOkTestWithCacheContent() throws AppException, IllegalArgumentException, IOException, Cache5XXException, Cache4XXException {

        Logger logger = Logger.getLogger("InfoLogging");
        
        Station station = Station.builder().stationCode("mockStationCode").brokerCode("mockBrokerCode").password("mockPwd").enabled(true).build();
        CreditorInstitutionStation creditorInstitutionStation = CreditorInstitutionStation.builder().creditorInstitutionCode("00595780131").stationCode("mockStationCode").build();
        CacheResponse cacheResponse = CacheResponse.builder()
        		.creditorInstitutionStations(Collections.singletonList(creditorInstitutionStation))
        		.stations(Collections.singletonList(station))
        		.build();
        

        String message = "{\"idPA\":\"00595780131\",\"flows\":[{\"identificativoFlusso\":\"2021-07-26AGID_02-S000000001\",\"dataOraFlusso\":1627293600000}], \"retry\": 0}";
        
        
        
        
        when(context.getLogger()).thenReturn(logger);
        when(function.getVars(anyString())).thenReturn("60");
        doReturn(cacheClient).when(function).getCacheClientInstance();
        lenient().doReturn(cacheResponse).when(cacheClient).getCache();
        FlowsService instance = spy(function.getFlowsServiceInstance(logger, "00595780131"));
        doReturn(instance).when(function).getFlowsServiceInstance(logger, "00595780131");
        

        function.run(message, context);

        verify(context, times(1)).getLogger();
        verify(instance, times(1)).flowsXmlDownloading(any(), anyString(), anyInt());
    }

    @Test
    void runWithInvalidMessageTest() {

        Logger logger = Logger.getLogger("InfoLogging");

        String message = "invalidMessage";
        when(context.getLogger()).thenReturn(logger);

        function.run(message, context);

        verify(context, times(1)).getLogger();
    }

    @Test
    void getFlowServiceInstanceTest() throws AppException, IllegalArgumentException, IOException, Cache5XXException, Cache4XXException {

        Logger logger = Logger.getLogger("testlogging");
        when(function.getVars(anyString())).thenReturn("60");
        
        Station station = Station.builder().stationCode("mockStationCode").brokerCode("mockBrokerCode").password("mockPwd").enabled(true).build();
        CreditorInstitutionStation creditorInstitutionStation = CreditorInstitutionStation.builder().creditorInstitutionCode("00595780131").stationCode("mockStationCode").build();
        CacheResponse cacheResponse = CacheResponse.builder()
        		.creditorInstitutionStations(Collections.singletonList(creditorInstitutionStation))
        		.stations(Collections.singletonList(station))
        		.build();
        
        doReturn(cacheClient).when(function).getCacheClientInstance();
        lenient().doReturn(cacheResponse).when(cacheClient).getCache();

        // test
        FlowsService instance = function.getFlowsServiceInstance(logger, "00595780131");

        Assertions.assertNotNull(instance);
    }

    @Test
    void runExceptionTest() throws AppException {

        Logger logger = Logger.getLogger("InfoLogging");

        String message = "{\"idPA\":\"00595780131\",\"flows\":[{\"identificativoFlusso\":\"2021-07-26AGID_02-S000000001\",\"dataOraFlusso\":1627293600000}]}";
        when(context.getLogger()).thenReturn(logger);

        doThrow(RuntimeException.class).when(function).getFlowsServiceInstance(logger, "00595780131");

        function.run(message, context);

        verify(context, times(1)).getLogger();
    }
}
