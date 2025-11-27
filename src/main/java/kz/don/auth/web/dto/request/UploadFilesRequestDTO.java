package kz.don.auth.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFilesRequestDTO {

    @NotBlank(message = "Folder name is required")
    private String folderName;

    @NotNull(message = "Documents list is required")
    private List<DocumentDTO> documents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentDTO {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Content is required")
        private String content;
    }
}
