package searchengine.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import searchengine.dto.SiteDto;
import searchengine.model.SiteModel;

@Mapper
public interface SiteMapper {

    SiteMapper INSTANCE = Mappers.getMapper(SiteMapper.class);

    SiteDto toDto(SiteModel site);

    SiteModel toEntity(SiteDto siteDto);
}
