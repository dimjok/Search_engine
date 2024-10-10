package searchengine.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import searchengine.dto.LemmaDto;
import searchengine.model.LemmaModel;

@Mapper
public interface LemmaMapper {
    LemmaMapper INSTANCE = Mappers.getMapper(LemmaMapper.class);

    LemmaDto toDto(LemmaModel lemmaModel);

    @Mapping(target = "site", source = "siteDto")
    LemmaModel toEntity(LemmaDto lemmaDto);
}
