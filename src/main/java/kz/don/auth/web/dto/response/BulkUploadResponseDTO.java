package kz.don.auth.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponseDTO {
    
    private String folderName;
    private String folderPageId;
    private String folderUrl;
    private List<UploadedDocument> documents;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadedDocument {
        private String title;
        private String pageId;
        private String url;
    }
}