package searchengine.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IndexDto {

    private Integer id;

    private PageDto pageDto;

    private LemmaDto lemmaDto;

    private Float rank;
}
