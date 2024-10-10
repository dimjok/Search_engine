package searchengine.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import searchengine.enums.StatusType;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
@Setter
@Builder
public class SiteDto {

    private Integer id;

    private StatusType status;

    private LocalDateTime statusTime;

    private String lastError;

    private String url;

    private String name;

    private Set<PageDto> pagesDto;

    private Set<LemmaDto> lemmasDto;
}
