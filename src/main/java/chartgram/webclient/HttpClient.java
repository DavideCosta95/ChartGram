package chartgram.webclient;

import chartgram.exceptions.ApiCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@Slf4j
public class HttpClient {

	private HttpClient() {
	}

	public byte[] doHttpGet(String baseUrl, Map<String, List<String>> queryParams) throws ApiCommunicationException {
		log.debug("baseUrl={}, queryParams={}", baseUrl, queryParams);

		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.defaultHeader(HttpHeaders.USER_AGENT, "ChartGram")
				.build();

		Function<UriBuilder, URI> uriFunction = uriBuilder -> {
			uriBuilder.path("");
			queryParams.forEach(uriBuilder::queryParam);
			return uriBuilder.build();
		};

		try {
			byte[] responseBean = webClient
					.get()
					.uri(uriFunction)
					.accept(MediaType.IMAGE_JPEG)
					.retrieve()
					.onStatus(HttpStatus::isError, this::errorCodesHandler)
					.bodyToMono(byte[].class)
					.onErrorMap(Predicate.not(ApiCommunicationException.class::isInstance), throwable -> new ApiCommunicationException("Error performing HTTP GET", throwable))
					.block();
			if (responseBean == null) {
				throw new ApiCommunicationException("Error performing HTTP GET: response null");
			}
			return responseBean;
		} catch (ApiCommunicationException e) {
			throw e;
		} catch (Exception e) {
			if (e.getCause() instanceof ApiCommunicationException) {
				throw (ApiCommunicationException) e.getCause();
			}
			throw new ApiCommunicationException(e);
		}
	}

	private Mono<? extends Throwable> errorCodesHandler(ClientResponse response) {
		return response.body(BodyExtractors.toDataBuffers())
				.reduce(DataBuffer::write)
				.map(dataBuffer -> {
					byte[] bytes = new byte[dataBuffer.readableByteCount()];
					dataBuffer.read(bytes);
					DataBufferUtils.release(dataBuffer);
					return bytes;
				})
				.defaultIfEmpty(new byte[0])
				.flatMap(bodyBytes -> {
					String msg = String.format("Endpoint response: %d - %s: %s", response.statusCode().value(), response.statusCode().getReasonPhrase(), new String(bodyBytes));
					return Mono.error(new ApiCommunicationException(msg));
				});
	}
}
