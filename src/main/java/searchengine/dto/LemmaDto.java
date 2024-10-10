package searchengine.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class LemmaDto {

    private Integer id;

    private SiteDto siteDto;

    private String lemma;

    private Integer frequency;
}
