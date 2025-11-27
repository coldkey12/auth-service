package kz.don.auth.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadRequestDTO {
    
    @NotBlank(message = "Folder name is required")
    private String folderName;
    
    @NotEmpty(message = "At least one document is required")
    private List<DocumentDTO> documents;
    
    private String spaceKey; // Optional, uses default if not provided
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentDTO {
        @NotBlank(message = "Document title is required")
        private String title;
        
        @NotBlank(message = "Document content is required")
        private String content;
    }
}