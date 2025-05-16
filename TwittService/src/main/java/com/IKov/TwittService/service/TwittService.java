package com.IKov.TwittService.service;

import com.IKov.TwittService.entity.TwittPost;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TwittService {

    boolean postTwitt(TwittPost twittPost);

    List<TwittPost> formRandomTwittStack(Integer n);

}
