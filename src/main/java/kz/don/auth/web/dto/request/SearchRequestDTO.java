package kz.don.auth.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDTO {

    private String query;
    private String spaceKey;
    private List<String> labels;
    private String type; // page, blogpost, attachment, etc.

    @Builder.Default
    private Integer limit = 25;

    @Builder.Default
    private Integer start = 0;
}