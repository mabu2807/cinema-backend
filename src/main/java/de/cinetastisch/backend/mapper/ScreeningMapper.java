package de.cinetastisch.backend.mapper;

import de.cinetastisch.backend.dto.ScreeningDto;
import de.cinetastisch.backend.dto.ScreeningRequestDto;
import de.cinetastisch.backend.model.Screening;
import de.cinetastisch.backend.service.MovieService;
import de.cinetastisch.backend.service.RoomService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ScreeningMapper {

    protected MovieService movieService;

    protected RoomService roomService;

    @Mapping(target = "endDateTime", source = "endDateTime", defaultExpression = "java(screeningRequestDto.startDateTime().plusMinutes(Long.parseLong(movieService.getMovie(screeningRequestDto.movieId()).getRuntime().split(\" \")[0])).plusMinutes(30L))")
//    @Mapping(target = "status", expression = "java(de.cinetastisch.backend.enumeration.ScreeningStatus.valueOf(screeningRequestDto.status()))")
    @Mapping(target = "status", defaultValue = "TICKET_SALE_OPEN")
    @Mapping(target = "room", expression = "java(roomService.getRoom(screeningRequestDto.roomId()))")
    @Mapping(target = "movie", expression = "java(movieService.getMovie(screeningRequestDto.movieId()))")
    @Mapping(target = "id", ignore = true)
    public abstract Screening screeningRequestDtoToEntity(ScreeningRequestDto screeningRequestDto);


    @Mapping(target = "movie", expression = "java(movieService.getMovie(screeningDto.movieId()))")
    @Mapping(target = "room", expression = "java(roomService.getRoom(screeningDto.roomId()))")
    @Mapping(target = "id", ignore = true)
    public abstract Screening dtoToEntity(ScreeningDto screeningDto);
}