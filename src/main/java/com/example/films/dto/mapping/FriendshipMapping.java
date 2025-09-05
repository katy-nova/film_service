package com.example.films.dto.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.films.dto.FriendshipDto;
import com.example.films.model.entity.Friendship;

@Mapper(componentModel = "spring")
public interface FriendshipMapping {

    @Mapping(target = "userLogin", expression = "java(friendship.getUser().getLogin())")
    @Mapping(target = "friendLogin", expression = "java(friendship.getFriend().getLogin())")
    FriendshipDto toFriendshipDto(Friendship friendship);
}
