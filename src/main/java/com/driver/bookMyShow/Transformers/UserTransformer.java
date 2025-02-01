package com.driver.bookMyShow.Transformers;

import com.driver.bookMyShow.Dtos.RequestDtos.UserEntryDto;
import com.driver.bookMyShow.Dtos.ResponseDtos.ReturnUserDto;
import com.driver.bookMyShow.Models.UserEntity;

public class UserTransformer {

    public static UserEntity userDtoToUser(UserEntryDto userEntryDto) {
        UserEntity user = UserEntity.builder()
                .name(userEntryDto.getName())
                .age(userEntryDto.getAge())
                .address(userEntryDto.getAddress())
                .gender(userEntryDto.getGender())
                .mobileNo(userEntryDto.getMobileNo())
                .emailId(userEntryDto.getEmailId())
                .password((userEntryDto.getPassword()))
                .role(userEntryDto.getRole())
                .build();

        return user;
    }

    public static ReturnUserDto userToUserDto(UserEntity user) {
        ReturnUserDto userDto = ReturnUserDto.builder()
                .name(user.getName())
                .age(user.getAge())
                .address(user.getAddress())
                .gender(user.getGender())
                .build();

        return userDto;
    }
}
