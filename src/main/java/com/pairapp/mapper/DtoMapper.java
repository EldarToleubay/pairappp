package com.pairapp.mapper;

import com.pairapp.dto.MoodRequestResponse;
import com.pairapp.dto.MoodResponseDto;
import com.pairapp.dto.PairResponse;
import com.pairapp.dto.UserSummaryDto;
import com.pairapp.entity.MoodRequest;
import com.pairapp.entity.MoodResponse;
import com.pairapp.entity.Pair;
import com.pairapp.entity.User;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static UserSummaryDto toUserSummary(User user) {
        return new UserSummaryDto(user.getId(), user.getName());
    }

    public static PairResponse toPairResponse(Pair pair, User partner) {
        return new PairResponse(pair.getId(), pair.getStatus(), pair.getCreatedAt(), toUserSummary(partner));
    }

    public static MoodRequestResponse toMoodRequestResponse(MoodRequest request) {
        return new MoodRequestResponse(request.getId(), request.getFromUserId(), request.getToUserId(),
                request.getStatus(), request.getCreatedAt(), request.getExpiresAt());
    }

    public static MoodResponseDto toMoodResponseDto(MoodResponse response) {
        return new MoodResponseDto(response.getId(), response.getRequestId(), response.getBaseFeeling(),
                response.getMode(), response.getAvoid(), response.getNotePreset(), response.getValidUntil(),
                response.getCreatedAt());
    }
}
