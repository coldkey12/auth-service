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
public class GetAllFilesResponseDTO {
    
    private List<FileDTO> files;
    private int totalCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDTO {
        private String id;
        private String title;
        private String content;
        private String url;
        private String folderName;
        private String createdDate;
    }
}