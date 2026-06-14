package com.bachiep.sems.dto.response;

import com.bachiep.sems.entity.LoginHistory;
import com.bachiep.sems.enums.LoginStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoginHistoryResponse {
    private Long id;
    private String username;
    private String ipAddress;
    private LoginStatus status;
    private LocalDateTime loginTime;

    public static LoginHistoryResponse from(LoginHistory loginHistory) {
        return LoginHistoryResponse.builder()
                .id(loginHistory.getId())
                .username(loginHistory.getUsername())
                .ipAddress(loginHistory.getIpAddress())
                .status(loginHistory.getStatus())
                .loginTime(loginHistory.getLoginTime())
                .build();
    }
}
