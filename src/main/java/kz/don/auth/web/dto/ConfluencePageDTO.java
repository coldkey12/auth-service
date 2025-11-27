package kz.don.auth.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluencePageDTO {

    private String id;
    private String type;
    private String status;
    private String title;

    @JsonProperty("space")
    private SpaceDTO space;

    @JsonProperty("body")
    private BodyDTO body;

    @JsonProperty("version")
    private VersionDTO version;

    @JsonProperty("_links")
    private LinksDTO links;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpaceDTO {
        private String key;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BodyDTO {
        @JsonProperty("storage")
        private StorageDTO storage;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StorageDTO {
            private String value;
            private String representation;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionDTO {
        private Integer number;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinksDTO {
        @JsonProperty("webui")
        private String webui;

        @JsonProperty("base")
        private String base;
    }
}
