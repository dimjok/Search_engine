package searchengine.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import searchengine.dto.IndexDto;
import searchengine.model.IndexModel;

@Mapper
public interface IndexMapper {
    IndexMapper INSTANCE = Mappers.getMapper(IndexMapper.class);

    IndexModel toEntity(IndexDto indexDto);

    IndexDto toDto(IndexModel indexModel);
}
