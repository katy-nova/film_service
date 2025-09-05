package com.example.films.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSimpleDto { // для отображения списка друзей/лайков

    private String name;
    private String login;
}
