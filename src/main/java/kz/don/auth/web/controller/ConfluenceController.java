package kz.don.auth.web.controller;

import jakarta.validation.Valid;
import kz.don.auth.application.service.ConfluenceService;
import kz.don.auth.web.dto.ConfluencePageDTO;
import kz.don.auth.web.dto.request.SearchRequestDTO;
import kz.don.auth.web.dto.request.CreatePageRequestDTO;
import kz.don.auth.web.dto.request.UpdatePageRequestDTO;
import kz.don.auth.web.dto.response.ConfluenceSearchResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/confluence")
@RequiredArgsConstructor
public class ConfluenceController {

    private final ConfluenceService confluenceService;

    /**
     * Search for pages
     * POST /api/confluence/search
     *
     * Body example:
     * {
     *   "query": "business requirements",
     *   "spaceKey": "PROJ",
     *   "labels": ["business-analysis", "requirements"],
     *   "limit": 25
     * }
     */
    @PostMapping("/search")
    public ResponseEntity<ConfluenceSearchResponseDTO> searchPages(@RequestBody SearchRequestDTO searchRequest) {
        try {
            log.info("Searching pages with request: {}", searchRequest);
            ConfluenceSearchResponseDTO response = confluenceService.searchPages(searchRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching pages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get page by ID
     * GET /api/confluence/pages/{pageId}
     */
    @GetMapping("/pages/{pageId}")
    public ResponseEntity<ConfluencePageDTO> getPage(@PathVariable String pageId) {
        try {
            log.info("Getting page with ID: {}", pageId);
            ConfluencePageDTO page = confluenceService.getPageById(pageId);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error("Error getting page", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get pages by space
     * GET /api/confluence/spaces/{spaceKey}/pages?limit=50
     */
    @GetMapping("/spaces/{spaceKey}/pages")
    public ResponseEntity<List<ConfluencePageDTO>> getPagesBySpace(
            @PathVariable String spaceKey,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {
        try {
            log.info("Getting pages from space: {}", spaceKey);
            List<ConfluencePageDTO> pages = confluenceService.getPagesBySpace(spaceKey, limit);
            return ResponseEntity.ok(pages);
        } catch (Exception e) {
            log.error("Error getting pages by space", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get pages by labels
     * GET /api/confluence/pages/by-labels?labels=business-analysis,requirements&spaceKey=PROJ
     */
    @GetMapping("/pages/by-labels")
    public ResponseEntity<List<ConfluencePageDTO>> getPagesByLabels(
            @RequestParam List<String> labels,
            @RequestParam(required = false) String spaceKey) {
        try {
            log.info("Getting pages with labels: {}", labels);
            List<ConfluencePageDTO> pages = confluenceService.getPagesByLabels(labels, spaceKey);
            return ResponseEntity.ok(pages);
        } catch (Exception e) {
            log.error("Error getting pages by labels", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new page
     * POST /api/confluence/pages
     *
     * Body example:
     * {
     *   "title": "Business Requirements Document",
     *   "content": "<p>This is the content</p>",
     *   "spaceKey": "PROJ",
     *   "parentPageId": "123456"
     * }
     */
    @PostMapping("/pages")
    public ResponseEntity<ConfluencePageDTO> createPage(@Valid @RequestBody CreatePageRequestDTO createRequest) {
        try {
            log.info("Creating page with title: {}", createRequest.getTitle());
            ConfluencePageDTO createdPage = confluenceService.createPage(createRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPage);
        } catch (Exception e) {
            log.error("Error creating page", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing page
     * PUT /api/confluence/pages/{pageId}
     *
     * Body example:
     * {
     *   "title": "Updated Business Requirements",
     *   "content": "<p>Updated content</p>",
     *   "currentVersion": 1
     * }
     */
    @PutMapping("/pages/{pageId}")
    public ResponseEntity<ConfluencePageDTO> updatePage(
            @PathVariable String pageId,
            @RequestBody UpdatePageRequestDTO updateRequest) {
        try {
            log.info("Updating page with ID: {}", pageId);
            ConfluencePageDTO updatedPage = confluenceService.updatePage(
                    pageId,
                    updateRequest.getTitle(),
                    updateRequest.getContent(),
                    updateRequest.getCurrentVersion()
            );
            return ResponseEntity.ok(updatedPage);
        } catch (Exception e) {
            log.error("Error updating page", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all spaces
     * GET /api/confluence/spaces?limit=100
     */
    @GetMapping("/spaces")
    public ResponseEntity<List<Map<String, Object>>> getAllSpaces(
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        try {
            log.info("Getting all spaces");
            List<Map<String, Object>> spaces = confluenceService.getAllSpaces(limit);
            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            log.error("Error getting spaces", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get specific space by key
     * GET /api/confluence/spaces/{spaceKey}
     */
    @GetMapping("/spaces/{spaceKey}")
    public ResponseEntity<Map<String, Object>> getSpace(@PathVariable String spaceKey) {
        try {
            log.info("Getting space: {}", spaceKey);
            Map<String, Object> space = confluenceService.getSpaceByKey(spaceKey);
            return ResponseEntity.ok(space);
        } catch (Exception e) {
            log.error("Error getting space", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Confluence Integration Service is running");
    }
}
