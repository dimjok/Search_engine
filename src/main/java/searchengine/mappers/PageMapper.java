package searchengine.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import searchengine.dto.PageDto;
import searchengine.model.PageModel;

@Mapper
public interface PageMapper {
    PageMapper INSTANCE = Mappers.getMapper(PageMapper.class);

    PageDto toDto(PageModel page);

    @Mapping(target = "site", source = "siteDto")
    PageModel toEntity(PageDto pageDto);

}
