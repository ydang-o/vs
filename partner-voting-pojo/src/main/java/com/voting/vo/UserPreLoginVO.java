package com.voting.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户预登录VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreLoginVO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String avatar;

    private Integer sex;

}
