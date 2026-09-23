package com.prudhvi.swacch.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Uploads collector photos to PixelBin (https://pixelbin.io) instead of
 * saving them into the local project folder.
 *
 * The upload uses the PixelBin "File Upload" API:
 *   POST https://api.pixelbin.io/service/platform/assets/v1.0/upload/direct
 * authenticated with the API token from the PixelBin dashboard.
 *
 * On success the asset's public CDN url (e.g.
 * https://cdn.pixelbin.io/v2/<cloud>/original/<path>/<name>)
 * is returned and stored in the waste_collections.photo_path column.
 */
@Service
public class PixelBinService {

	private static final Logger logger = LoggerFactory.getLogger(PixelBinService.class);

	private static final String UPLOAD_URL = "https://api.pixelbin.io/service/platform/assets/v1.0/upload/direct";

	@Value("${pixelbin.api-token}")
	private String apiToken;

	/** Folder inside PixelBin where waste photos are organised. */
	@Value("${pixelbin.folder:swacch/waste-photos}")
	private String folder;

	private final RestClient restClient;

	public PixelBinService(RestClient.Builder builder) {
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
		requestFactory.setReadTimeout(Duration.ofSeconds(60));
		this.restClient = builder.requestFactory(requestFactory).build();
	}

	/**
	 * Uploads an image to PixelBin and returns its public CDN url.
	 *
	 * @param data        raw image bytes
	 * @param filename    filename (with extension) for the asset
	 * @param contentType mime type of the image, e.g. image/png
	 * @return public CDN url of the uploaded asset
	 */
	public String upload(byte[] data, String filename, String contentType) {
		ByteArrayResource filePart = new ByteArrayResource(data) {
			@Override
			public String getFilename() {
				return filename;
			}
		};

		HttpHeaders fileHeaders = new HttpHeaders();
		fileHeaders.setContentType(MediaType.parseMediaType(contentType));

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new HttpEntity<>(filePart, fileHeaders));
		body.add("path", folder);
		body.add("name", stripExtension(filename));
		body.add("access", "public-read");
		body.add("overwrite", "false");
		body.add("filenameOverride", "true");

		try {
			Map<String, Object> response = restClient.post()
					.uri(UPLOAD_URL)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(body)
					.retrieve()
					.body(new ParameterizedTypeReference<Map<String, Object>>() {
					});

			if (response == null || response.get("url") == null || response.get("url").toString().isBlank()) {
					logger.error("PixelBin upload returned no url for file {}", filename);
					throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
							"Photo upload to PixelBin returned no url");
			}

			String url = response.get("url").toString();
			logger.debug("Uploaded photo to PixelBin: {}", url);
			return url;
		} catch (RestClientException e) {
			logger.error("PixelBin upload failed for file {}", filename, e);
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload photo to PixelBin", e);
		}
	}

	private static String stripExtension(String filename) {
		int dot = filename.lastIndexOf('.');
		return dot > 0 ? filename.substring(0, dot) : filename;
	}
}
