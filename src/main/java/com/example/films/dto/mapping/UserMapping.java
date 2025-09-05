package com.example.films.dto.mapping;

import org.mapstruct.Mapper;
import com.example.films.dto.user.UserCreateDto;
import com.example.films.dto.user.UserDto;
import com.example.films.dto.user.NewUserDto;
import com.example.films.dto.user.UserSimpleDto;
import com.example.films.model.entity.User;

@Mapper(componentModel = "spring", uses = ReviewMapping.class)
public interface UserMapping {
    UserDto toDto(User user);

    User fromDto(UserDto userDto);

    NewUserDto toNewUserDto(User user);

    UserSimpleDto toDtoFriend(User user);

    User fromDtoFriend(UserSimpleDto userSimpleDto);

    User fromCreateDto(UserCreateDto userCreateDto);
}
