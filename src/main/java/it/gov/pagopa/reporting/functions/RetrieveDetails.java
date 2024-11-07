package it.gov.pagopa.reporting.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

import it.gov.pagopa.reporting.client.ApiConfigClient;
import it.gov.pagopa.reporting.exception.AppException;
import it.gov.pagopa.reporting.models.FlowsMessage;
import it.gov.pagopa.reporting.models.cache.CacheResponse;
import it.gov.pagopa.reporting.models.cache.CreditorInstitutionStation;
import it.gov.pagopa.reporting.models.cache.Station;
import it.gov.pagopa.reporting.service.FlowsService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * FlowsXmlDownloadFunction Azure Functions with Azure Queue trigger.
 */
public class RetrieveDetails {
	
	private static CacheResponse cacheContent;
	private static final Lock cacheLock = new ReentrantLock();
	
    /**
     * This function will be invoked when a new message is detected in the queue
     * FLOWS_QUEUE related to FLOW_SA_CONNECTION_STRING (app settings)
     */
    @FunctionName("RetrieveDetailsFunction")
    public void run(
            @QueueTrigger(name = "RetrieveDetailsTrigger", queueName = "%FLOWS_QUEUE%", connection = "FLOW_SA_CONNECTION_STRING") String message,
            final ExecutionContext context) {

        Logger logger = context.getLogger();

        try {

            logger.log(Level.INFO, () -> "[FlowsDownloadFunction START]  processed a message " + message);

            FlowsMessage flows = new ObjectMapper().readValue(message, FlowsMessage.class);

            // retrieve fdr from node
            this.getFlowsServiceInstance(logger, flows.getIdPA())
                    .flowsXmlDownloading(Arrays.asList(flows.getFlows()), flows.getIdPA(), flows.getRetry() + 1);

            logger.log(Level.INFO, () -> "[FlowsDownloadFunction END]  processed a message " + message);
        } catch (JsonProcessingException em) {

            logger.log(Level.SEVERE, () -> "[FlowsDownloadFunction Error] Invalid Message Queue " + em.getMessage()
                    + " - message " + message);
        } catch (Exception e) {

            logger.log(Level.SEVERE, () -> "[FlowsDownloadFunction Error] Generic Error " + e.getMessage() + " "
                    + e.getCause() + " - message " + message);
        }
    }

    public FlowsService getFlowsServiceInstance(Logger logger, String idPA) throws AppException {
        String maxRetryQueuing = getVars("MAX_RETRY_QUEUING");
        String queueRetentionSec = getVars("QUEUE_RETENTION_SEC");
        String queueDelaySec = getVars("QUEUE_DELAY_SEC");
        
        ApiConfigClient cacheClient = this.getCacheClientInstance();
        // Check if cache update is needed and attempt to acquire lock
        if (isCacheUpdateNeeded() && (cacheLock.tryLock())) {
                try {
                    // Double-check if cache still needs updating within the lock.
                    if (isCacheUpdateNeeded()) {
                    	RetrieveDetails.setCache(cacheClient);
                    }
                } finally {
                    cacheLock.unlock();
                }
            
        }
        
        logger.log(Level.INFO, () -> "[RetrieveDetails][Config-Cache][Start] idPa: " + idPA);
        Station stationBroker = getPAStationIntermediario(idPA)
                .orElseThrow(() -> new AppException(String.format("No data present in api config database for PA %s", idPA)));
        String idBroker = stationBroker.getBrokerCode();
        String idStation = stationBroker.getStationCode();
        String stationPassword = stationBroker.getPassword();
        
        return new FlowsService(System.getenv("FLOW_SA_CONNECTION_STRING"), idBroker,
        		idStation, stationPassword, System.getenv("FLOWS_XML_BLOB"), System.getenv("FLOWS_QUEUE"),
                System.getenv("FLOWS_TABLE"), Integer.parseInt(maxRetryQueuing), Integer.parseInt(queueRetentionSec), Integer.parseInt(queueDelaySec),
                logger);
    }

    public String getVars(String vars) {
        return System.getenv(vars);
    }
    
    public ApiConfigClient getCacheClientInstance() {
        return ApiConfigClient.getInstance();
    }
    
    public static void setCacheContent(CacheResponse cacheContent) {
		RetrieveDetails.cacheContent = cacheContent;
	}
    
    private boolean isCacheUpdateNeeded() {
        return cacheContent == null || (cacheContent.getRetrieveDate() != null &&
                cacheContent.getRetrieveDate().isBefore(LocalDate.now()));
    }
    
    private static void setCache(ApiConfigClient cacheClient) throws AppException {
        try {
            cacheContent = cacheClient.getCache();
            cacheContent.setRetrieveDate(LocalDate.now());
        } catch (Exception e) {
            cacheContent = null;
            throw new AppException(e.getMessage());
        }
    }
    
    private Optional<Station> getPAStationIntermediario(String idPa) {
        List<String> stationPa = getStations(idPa);
        return cacheContent.getStations().stream()
                .filter(station -> stationPa.contains(station.getStationCode()))
                .filter(Station::getEnabled)
                .findFirst();
    }
    
    private List<String> getStations(String idPa) {
        return cacheContent.getCreditorInstitutionStations().stream()
                .filter(creditorInstitutionStation -> creditorInstitutionStation.getCreditorInstitutionCode().equals(idPa))
                .map(CreditorInstitutionStation::getStationCode).collect(Collectors.toList());
    }
}
