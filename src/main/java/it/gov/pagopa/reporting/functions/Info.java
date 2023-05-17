package it.gov.pagopa.reporting.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.util.Optional;
import java.util.logging.Level;

public class Info {

	@FunctionName("ReportingServiceInfoFunction")
	public HttpResponseMessage run (
			@HttpTrigger(
					name = "InfoTrigger",
					methods = {HttpMethod.GET},
					route = "info",
					authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			final ExecutionContext context) {

		context.getLogger().log(Level.INFO, "Invoked health check HTTP trigger for pagopa-gpd-reporting-service.");
		return request.createResponseBuilder(HttpStatus.OK)
					   .header("Content-Type", "application/json")
					   .build();
	}
}
