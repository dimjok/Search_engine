package searchengine.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class PageDto {

    private Integer id;

    private SiteDto siteDto;

    private String path;

    private int code;

    private String content;

    private Set<IndexDto> indexesDto;
}
