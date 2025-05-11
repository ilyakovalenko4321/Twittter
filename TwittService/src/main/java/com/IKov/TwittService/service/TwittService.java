package com.IKov.TwittService.service;

import com.IKov.TwittService.entity.TwittPost;
import org.springframework.http.ResponseEntity;

public interface TwittService {

    boolean postTwitt(TwittPost twittPost);

}
