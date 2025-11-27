package kz.don.auth.web.controller;

import jakarta.validation.Valid;
import kz.don.auth.application.service.ConfluenceService;
import kz.don.auth.config.ConfluenceProperties;
import kz.don.auth.web.dto.request.UploadFilesRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/django")
@PreAuthorize("hasAnyRole('AUTHORITY', 'CLIENT', 'ANALYTIC')")
@RequiredArgsConstructor
public class DjangoIntegrationController {

    private final ConfluenceService confluenceService;
    private final ConfluenceProperties properties;

    /**
     * Upload files from Django to Confluence
     * POST /api/django/upload
     * 
     * Request Body:
     * {
     *   "folderName": "My Folder",
     *   "documents": [
     *     {
     *       "title": "Document 1",
     *       "content": "<h1>Content</h1>"
     *     },
     *     {
     *       "title": "Document 2",
     *       "content": "<p>More content</p>"
     *     }
     *   ]
     * }
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFiles(@Valid @RequestBody UploadFilesRequestDTO request) {
        try {
            log.info("Received upload request for folder: {} with {} documents", 
                    request.getFolderName(), request.getDocuments().size());
            
            // Convert DTO to service format
            List<Map<String, String>> documents = request.getDocuments().stream()
                    .map(doc -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("title", doc.getTitle());
                        map.put("content", doc.getContent());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            // Upload to Confluence
            var uploadedPages = confluenceService.uploadFilesToFolder(request.getFolderName(), documents);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("folderName", request.getFolderName());
            response.put("folderId", uploadedPages.get(0).getId());
            response.put("folderUrl", uploadedPages.get(0).getLinks().getWebui());
            response.put("documentsUploaded", uploadedPages.size() - 1); // -1 for folder itself
            response.put("message", "Successfully uploaded " + (uploadedPages.size() - 1) + " documents to folder '" + request.getFolderName() + "'");
            
            log.info("Upload successful: {} documents uploaded to folder '{}'", 
                    uploadedPages.size() - 1, request.getFolderName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error uploading files", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all files from Confluence
     * GET /api/django/files
     */
    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> getAllFiles() {
        try {
            log.info("Fetching all files from Confluence");
            
            List<Map<String, Object>> files = confluenceService.getAllFiles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalCount", files.size());
            response.put("files", files);
            
            log.info("Retrieved {} files", files.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching files", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all Confluence spaces
     * GET /api/django/spaces
     */
    @GetMapping("/spaces")
    public ResponseEntity<Map<String, Object>> getAllSpaces() {
        try {
            log.info("Fetching all Confluence spaces");

            List<Map<String, Object>> spaces = confluenceService.getAllSpaces(100);

            // Transform to simpler format with just what Django needs
            List<Map<String, String>> simplifiedSpaces = spaces.stream()
                    .map(space -> {
                        Map<String, String> simple = new HashMap<>();
                        simple.put("key", (String) space.get("key"));
                        simple.put("name", (String) space.get("name"));
                        simple.put("type", (String) space.get("type"));

                        // Get web UI link
                        Map<String, Object> links = (Map<String, Object>) space.get("_links");
                        if (links != null) {
                            String webui = (String) links.get("webui");
                            simple.put("url", properties.getBaseUrl() + webui);
                        }

                        return simple;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalCount", simplifiedSpaces.size());
            response.put("spaces", simplifiedSpaces);

            log.info("Retrieved {} spaces", simplifiedSpaces.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching spaces", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check for Django integration
     * GET /api/django/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Django Integration");
        return ResponseEntity.ok(response);
    }
}