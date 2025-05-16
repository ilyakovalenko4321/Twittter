package com.IKov.TimelineService.web.mapper;

import com.IKov.TimelineService.GetTwittsProto;
import com.IKov.TimelineService.entity.TwittPost;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class TwittPostMapper {
    public List<TwittPost> toDomainList(GetTwittsProto.GetTwittReply reply) {
        return reply.getTwittList().stream()
                .map(TwittPostMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Конвертирует один элемент Twitt → TwittPost.
     */
    private TwittPost toDomain(GetTwittsProto.Twitt proto) {
        TwittPost post = new TwittPost();
        // Если в protobuf есть поле twittId, то можно добавить:
        // post.setTwittId(UUID.fromString(proto.getTwittId()));
        // Но раз у вас его нет — оставляем null
        post.setUserTag(proto.getUserTag());
        post.setTwittText(proto.getTwittText());
        post.setTwittHeader(proto.getTwittHeader());
        post.setTwittTags(proto.getTwittTagsList());

        // Конвертация Timestamp → LocalDateTime (UTC)
        Instant inst = Instant.ofEpochSecond(
                proto.getCreatedAt().getSeconds(),
                proto.getCreatedAt().getNanos()
        );
        post.setCreatedAt(LocalDateTime.ofInstant(inst, ZoneOffset.UTC));

        return post;
    }

}
