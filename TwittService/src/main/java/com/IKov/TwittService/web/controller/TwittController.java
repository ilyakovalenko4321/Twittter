package com.IKov.TwittService.web.controller;

import com.IKov.TwittService.entity.TwittPost;
import com.IKov.TwittService.service.TwittService;
import com.IKov.TwittService.web.dto.TwittPostDto;
import com.IKov.TwittService.web.mapper.TwittPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/twitt")
@RequiredArgsConstructor
public class TwittController {

    private final TwittService twittService;
    private final TwittPostMapper twittPostMapper;

    @PostMapping("/post")
    public ResponseEntity<?> create(@RequestBody TwittPostDto twittPostDto){
        TwittPost twittPost = twittPostMapper.toEntity(twittPostDto);
        boolean isSuccessfullySaved = twittService.postTwitt(twittPost);

        if(isSuccessfullySaved){
            return ResponseEntity.ok().build();
        } else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
