package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BirthdayPartyPhotoVO {

    private Long id;
    private Long partyId;
    private String photoUrl;
    private Long uploadedBy;
    private String uploadedByName;
    private Integer likeCount;
    private Boolean hasLiked;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
