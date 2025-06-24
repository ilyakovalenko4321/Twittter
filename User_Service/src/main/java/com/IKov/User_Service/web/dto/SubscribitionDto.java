package com.IKov.User_Service.web.dto;

import lombok.Data;

@Data
public class SubscribitionDto {

    private String subject;
    private String object;
    private boolean subscribe; //true - да false - нет(отписался)

}
