package com.bachiep.sems.service;

import com.bachiep.sems.dto.response.LoginHistoryResponse;
import com.bachiep.sems.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    public Page<LoginHistoryResponse> getLoginHistory(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        return loginHistoryRepository.findAllByOrderByLoginTimeDesc(PageRequest.of(safePage, safeSize))
                .map(LoginHistoryResponse::from);
    }
}
