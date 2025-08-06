package com.checkmate.bub.user.mapper;

import com.checkmate.bub.auth.dto.KakaoUserInfoResponseDto;
import com.checkmate.bub.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id", target = "kakaoId")
    @Mapping(source = "kakaoAccount.profile.nickname", target = "nickname")
    @Mapping(source = "kakaoAccount.profile.profileImageUrl", target = "profileImageUrl")
    User kakaoDtoToUser(KakaoUserInfoResponseDto kakaoUserInfo);
}
