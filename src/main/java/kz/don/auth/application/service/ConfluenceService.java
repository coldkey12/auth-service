package kz.don.auth.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.don.auth.config.ConfluenceProperties;
import kz.don.auth.web.dto.ConfluencePageDTO;
import kz.don.auth.web.dto.request.BulkUploadRequestDTO;
import kz.don.auth.web.dto.request.CreatePageRequestDTO;
import kz.don.auth.web.dto.request.SearchRequestDTO;
import kz.don.auth.web.dto.response.BulkUploadResponseDTO;
import kz.don.auth.web.dto.response.ConfluenceSearchResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfluenceService {

    private final CloseableHttpClient httpClient;
    private final ConfluenceProperties properties;
    private final ObjectMapper objectMapper;

    private static final String API_PATH = "/rest/api";

    /**
     * Search for pages in Confluence by various criteria
     */
    public ConfluenceSearchResponseDTO searchPages(SearchRequestDTO searchRequest) throws IOException {
        StringBuilder cqlBuilder = new StringBuilder();

        // Build CQL (Confluence Query Language) query
        List<String> conditions = new ArrayList<>();

        if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
            conditions.add(String.format("text ~ \"%s\"", searchRequest.getQuery()));
        }

        if (searchRequest.getSpaceKey() != null && !searchRequest.getSpaceKey().isEmpty()) {
            conditions.add(String.format("space = \"%s\"", searchRequest.getSpaceKey()));
        }

        if (searchRequest.getType() != null && !searchRequest.getType().isEmpty()) {
            conditions.add(String.format("type = %s", searchRequest.getType()));
        } else {
            conditions.add("type = page");
        }

        if (searchRequest.getLabels() != null && !searchRequest.getLabels().isEmpty()) {
            String labelConditions = searchRequest.getLabels().stream()
                    .map(label -> String.format("label = \"%s\"", label))
                    .collect(Collectors.joining(" AND "));
            conditions.add("(" + labelConditions + ")");
        }

        String cql = String.join(" AND ", conditions);

        String url = String.format("%s%s/content/search?cql=%s&limit=%d&start=%d&expand=body.storage,version,space",
                properties.getBaseUrl(),
                API_PATH,
                encodeValue(cql),
                searchRequest.getLimit(),
                searchRequest.getStart());

        log.info("Searching Confluence with CQL: {}", cql);

        HttpGet request = new HttpGet(url);
        addAuthHeader(request);

        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            log.debug("Search response: {}", responseBody);

            if (response.getCode() >= 200 && response.getCode() < 300) {
                return objectMapper.readValue(responseBody, ConfluenceSearchResponseDTO.class);
            } else {
                log.error("Search failed with status {}: {}", response.getCode(), responseBody);
                throw new IOException("Failed to search pages: " + response.getCode());
            }
        });
    }

    /**
     * Get a specific page by ID
     */
    public ConfluencePageDTO getPageById(String pageId) throws IOException {
        String url = String.format("%s%s/content/%s?expand=body.storage,version,space",
                properties.getBaseUrl(),
                API_PATH,
                pageId);

        log.info("Fetching page with ID: {}", pageId);

        HttpGet request = new HttpGet(url);
        addAuthHeader(request);

        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());

            if (response.getCode() >= 200 && response.getCode() < 300) {
                return objectMapper.readValue(responseBody, ConfluencePageDTO.class);
            } else {
                log.error("Failed to get page with status {}: {}", response.getCode(), responseBody);
                throw new IOException("Failed to get page: " + response.getCode());
            }
        });
    }

    /**
     * Create a new page in Confluence
     */
    public ConfluencePageDTO createPage(CreatePageRequestDTO createRequest) throws IOException {
        String spaceKey = createRequest.getSpaceKey() != null ?
                createRequest.getSpaceKey() : properties.getDefaultSpaceKey();

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "page");
        payload.put("title", createRequest.getTitle());

        Map<String, String> space = new HashMap<>();
        space.put("key", spaceKey);
        payload.put("space", space);

        Map<String, Object> body = new HashMap<>();
        Map<String, String> storage = new HashMap<>();
        storage.put("value", createRequest.getContent());
        storage.put("representation", createRequest.getRepresentation());
        body.put("storage", storage);
        payload.put("body", body);

        // Add parent page if specified
        if (createRequest.getParentPageId() != null && !createRequest.getParentPageId().isEmpty()) {
            List<Map<String, String>> ancestors = new ArrayList<>();
            Map<String, String> parent = new HashMap<>();
            parent.put("id", createRequest.getParentPageId());
            ancestors.add(parent);
            payload.put("ancestors", ancestors);
        }

        String url = String.format("%s%s/content", properties.getBaseUrl(), API_PATH);

        log.info("Creating page with title: {}", createRequest.getTitle());

        HttpPost request = new HttpPost(url);
        addAuthHeader(request);
        request.setHeader("Content-Type", "application/json");

        String jsonPayload = objectMapper.writeValueAsString(payload);
        request.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());

            if (response.getCode() >= 200 && response.getCode() < 300) {
                log.info("Page created successfully");
                return objectMapper.readValue(responseBody, ConfluencePageDTO.class);
            } else {
                log.error("Failed to create page with status {}: {}", response.getCode(), responseBody);
                throw new IOException("Failed to create page: " + response.getCode() + " - " + responseBody);
            }
        });
    }

    /**
     * Update an existing page
     */
    public ConfluencePageDTO updatePage(String pageId, String title, String content, Integer currentVersion) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "page");
        payload.put("title", title);

        Map<String, Object> body = new HashMap<>();
        Map<String, String> storage = new HashMap<>();
        storage.put("value", content);
        storage.put("representation", "storage");
        body.put("storage", storage);
        payload.put("body", body);

        Map<String, Object> version = new HashMap<>();
        version.put("number", currentVersion + 1);
        payload.put("version", version);

        String url = String.format("%s%s/content/%s", properties.getBaseUrl(), API_PATH, pageId);

        log.info("Updating page with ID: {}", pageId);

        HttpPut request = new HttpPut(url);
        addAuthHeader(request);
        request.setHeader("Content-Type", "application/json");

        String jsonPayload = objectMapper.writeValueAsString(payload);
        request.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());

            if (response.getCode() >= 200 && response.getCode() < 300) {
                log.info("Page updated successfully");
                return objectMapper.readValue(responseBody, ConfluencePageDTO.class);
            } else {
                log.error("Failed to update page with status {}: {}", response.getCode(), responseBody);
                throw new IOException("Failed to update page: " + response.getCode() + " - " + responseBody);
            }
        });
    }

    /**
     * Get all pages from a specific space
     */
    public List<ConfluencePageDTO> getPagesBySpace(String spaceKey, Integer limit) throws IOException {
        SearchRequestDTO searchRequest = SearchRequestDTO.builder()
                .spaceKey(spaceKey)
                .type("page")
                .limit(limit != null ? limit : 100)
                .build();

        ConfluenceSearchResponseDTO response = searchPages(searchRequest);
        return response.getResults();
    }

    /**
     * Search pages by labels
     */
    public List<ConfluencePageDTO> getPagesByLabels(List<String> labels, String spaceKey) throws IOException {
        SearchRequestDTO searchRequest = SearchRequestDTO.builder()
                .labels(labels)
                .spaceKey(spaceKey)
                .type("page")
                .limit(50)
                .build();

        ConfluenceSearchResponseDTO response = searchPages(searchRequest);
        return response.getResults();
    }

    /**
     * Upload multiple documents to Confluence in a folder structure
     * Creates a parent page (folder) and child pages (documents)
     */
    public List<ConfluencePageDTO> uploadFilesToFolder(String folderName, List<Map<String, String>> documents) throws IOException {
        String spaceKey = properties.getDefaultSpaceKey();

        // Step 1: Create parent page (folder)
        log.info("Creating folder: {}", folderName);
        Map<String, Object> folderPayload = new HashMap<>();
        folderPayload.put("type", "page");
        folderPayload.put("title", folderName);

        Map<String, String> space = new HashMap<>();
        space.put("key", spaceKey);
        folderPayload.put("space", space);

        Map<String, Object> body = new HashMap<>();
        Map<String, String> storage = new HashMap<>();
        storage.put("value", "<p>This folder contains uploaded documents.</p>");
        storage.put("representation", "storage");
        body.put("storage", storage);
        folderPayload.put("body", body);

        String folderUrl = String.format("%s%s/content", properties.getBaseUrl(), API_PATH);
        HttpPost folderRequest = new HttpPost(folderUrl);
        addAuthHeader(folderRequest);
        folderRequest.setHeader("Content-Type", "application/json");

        String folderJson = objectMapper.writeValueAsString(folderPayload);
        folderRequest.setEntity(new StringEntity(folderJson, ContentType.APPLICATION_JSON));

        ConfluencePageDTO folderPage = httpClient.execute(folderRequest, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            if (response.getCode() >= 200 && response.getCode() < 300) {
                return objectMapper.readValue(responseBody, ConfluencePageDTO.class);
            } else {
                throw new IOException("Failed to create folder: " + response.getCode());
            }
        });

        String folderId = folderPage.getId();
        log.info("Folder created with ID: {}", folderId);

        // Step 2: Create child pages (documents)
        List<ConfluencePageDTO> uploadedPages = new ArrayList<>();
        uploadedPages.add(folderPage);

        for (Map<String, String> doc : documents) {
            String title = doc.get("title");
            String content = doc.get("content");

            log.info("Creating document: {}", title);

            Map<String, Object> docPayload = new HashMap<>();
            docPayload.put("type", "page");
            docPayload.put("title", title);
            docPayload.put("space", space);

            Map<String, Object> docBody = new HashMap<>();
            Map<String, String> docStorage = new HashMap<>();
            docStorage.put("value", content);
            docStorage.put("representation", "storage");
            docBody.put("storage", docStorage);
            docPayload.put("body", docBody);

            // Set parent to folder
            List<Map<String, String>> ancestors = new ArrayList<>();
            Map<String, String> parent = new HashMap<>();
            parent.put("id", folderId);
            ancestors.add(parent);
            docPayload.put("ancestors", ancestors);

            String docUrl = String.format("%s%s/content", properties.getBaseUrl(), API_PATH);
            HttpPost docRequest = new HttpPost(docUrl);
            addAuthHeader(docRequest);
            docRequest.setHeader("Content-Type", "application/json");

            String docJson = objectMapper.writeValueAsString(docPayload);
            docRequest.setEntity(new StringEntity(docJson, ContentType.APPLICATION_JSON));

            ConfluencePageDTO createdDoc = httpClient.execute(docRequest, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getCode() >= 200 && response.getCode() < 300) {
                    return objectMapper.readValue(responseBody, ConfluencePageDTO.class);
                } else {
                    log.error("Failed to create document: {}", responseBody);
                    throw new IOException("Failed to create document: " + response.getCode());
                }
            });

            uploadedPages.add(createdDoc);
        }

        log.info("Successfully uploaded {} documents to folder '{}'", documents.size(), folderName);
        return uploadedPages;
    }

    /**
     * Get all files from Confluence (all pages in the default space)
     */
    public List<Map<String, Object>> getAllFiles() throws IOException {
        String spaceKey = properties.getDefaultSpaceKey();
        String url = String.format("%s%s/content?spaceKey=%s&limit=500&expand=body.storage,version",
                properties.getBaseUrl(),
                API_PATH,
                spaceKey);

        log.info("Fetching all files from space: {}", spaceKey);

        HttpGet request = new HttpGet(url);
        addAuthHeader(request);

        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());

            if (response.getCode() >= 200 && response.getCode() < 300) {
                Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
                List<Map<String, Object>> pages = (List<Map<String, Object>>) result.get("results");

                // Transform to simpler format
                List<Map<String, Object>> files = new ArrayList<>();
                for (Map<String, Object> page : pages) {
                    Map<String, Object> file = new HashMap<>();
                    file.put("id", page.get("id"));
                    file.put("title", page.get("title"));

                    Map<String, Object> bodyMap = (Map<String, Object>) page.get("body");
                    if (bodyMap != null && bodyMap.containsKey("storage")) {
                        Map<String, Object> storage = (Map<String, Object>) bodyMap.get("storage");
                        file.put("content", storage.get("value"));
                    }

                    Map<String, Object> links = (Map<String, Object>) page.get("_links");
                    if (links != null) {
                        file.put("url", properties.getBaseUrl() + links.get("webui"));
                    }

                    files.add(file);
                }

                return files;
            } else {
                throw new IOException("Failed to get files: " + response.getCode());
            }
        });
    }

    private void addAuthHeader(org.apache.hc.core5.http.HttpRequest request) {
        String auth = properties.getUsername() + ":" + properties.getApiToken();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        request.setHeader("Authorization", "Basic " + encodedAuth);
    }

    /**
     * Bulk upload: Create folder page and upload multiple documents under it
     */
    public BulkUploadResponseDTO bulkUpload(BulkUploadRequestDTO request) throws IOException {
        String spaceKey = request.getSpaceKey() != null ?
                request.getSpaceKey() : properties.getDefaultSpaceKey();

        // Step 1: Create folder (parent page)
        CreatePageRequestDTO folderRequest = CreatePageRequestDTO.builder()
                .title(request.getFolderName())
                .content("<p>Folder containing uploaded documents</p>")
                .spaceKey(spaceKey)
                .build();

        ConfluencePageDTO folderPage = createPage(folderRequest);
        log.info("Created folder page: {} with ID: {}", folderPage.getTitle(), folderPage.getId());

        // Step 2: Upload all documents under the folder
        List<BulkUploadResponseDTO.UploadedDocument> uploadedDocs = new ArrayList<>();

        for (BulkUploadRequestDTO.DocumentDTO doc : request.getDocuments()) {
            CreatePageRequestDTO docRequest = CreatePageRequestDTO.builder()
                    .title(doc.getTitle())
                    .content(doc.getContent())
                    .spaceKey(spaceKey)
                    .parentPageId(folderPage.getId()) // Set folder as parent
                    .build();

            ConfluencePageDTO uploadedPage = createPage(docRequest);

            uploadedDocs.add(BulkUploadResponseDTO.UploadedDocument.builder()
                    .title(uploadedPage.getTitle())
                    .pageId(uploadedPage.getId())
                    .url(properties.getBaseUrl() + uploadedPage.getLinks().getWebui())
                    .build());

            log.info("Uploaded document: {} under folder: {}", uploadedPage.getTitle(), folderPage.getTitle());
        }

        return BulkUploadResponseDTO.builder()
                .folderName(folderPage.getTitle())
                .folderPageId(folderPage.getId())
                .folderUrl(properties.getBaseUrl() + folderPage.getLinks().getWebui())
                .documents(uploadedDocs)
                .build();
    }

    /**
     * Get all documents (pages) from Confluence
     */
    public List<Map<String, Object>> getAllDocuments(Integer limit) throws IOException {
        String url = String.format("%s%s/content?limit=%d&expand=body.storage,version,space",
                properties.getBaseUrl(),
                API_PATH,
                limit != null ? limit : 100);

        log.info("Fetching all documents");

        HttpGet request = new HttpGet(url);
        addAuthHeader(request);

        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());

            if (response.getCode() >= 200 && response.getCode() < 300) {
                Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
                return (List<Map<String, Object>>) result.get("results");
            } else {
                log.error("Failed to get documents with status {}: {}", response.getCode(), responseBody);
                throw new IOException("Failed to get documents: " + response.getCode());
            }
        });
    }

    /**
     * Get all spaces
     */
    public List<Map<String, Object>> getAllSpaces(Integer limit) throws IOException {
        String url = String.format("%s%s/space?limit=%d&expand=description.plain,homepage",
                properties.getBaseUrl(),
                API_PATH,
                limit != null ? limit : 100);

        log.info("Fetching all spaces");

        HttpGet request = new HttpGet(url);
        addAuthHeader(request);

        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());

            if (response.getCode() >= 200 && response.getCode() < 300) {
                Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
                return (List<Map<String, Object>>) result.get("results");
            } else {
                log.error("Failed to get spaces with status {}: {}", response.getCode(), responseBody);
                throw new IOException("Failed to get spaces: " + response.getCode());
            }
        });
    }

    /**
     * Get a specific space by key
     */
    public Map<String, Object> getSpaceByKey(String spaceKey) throws IOException {
        String url = String.format("%s%s/space/%s?expand=description.plain,homepage",
                properties.getBaseUrl(),
                API_PATH,
                spaceKey);

        log.info("Fetching space with key: {}", spaceKey);

        HttpGet request = new HttpGet(url);
        addAuthHeader(request);

        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());

            if (response.getCode() >= 200 && response.getCode() < 300) {
                return objectMapper.readValue(responseBody, Map.class);
            } else {
                log.error("Failed to get space with status {}: {}", response.getCode(), responseBody);
                throw new IOException("Failed to get space: " + response.getCode());
            }
        });
    }

    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}

