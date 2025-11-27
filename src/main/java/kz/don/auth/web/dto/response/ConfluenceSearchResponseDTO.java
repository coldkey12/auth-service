package kz.don.auth.web.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import kz.don.auth.web.dto.ConfluencePageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceSearchResponseDTO {

    @JsonProperty("results")
    private List<ConfluencePageDTO> results;

    @JsonProperty("start")
    private Integer start;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("size")
    private Integer size;

    @JsonProperty("_links")
    private LinksDTO links;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinksDTO {
        @JsonProperty("next")
        private String next;

        @JsonProperty("base")
        private String base;
    }
}
