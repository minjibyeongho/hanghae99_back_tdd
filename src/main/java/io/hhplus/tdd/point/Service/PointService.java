package io.hhplus.tdd.point.Service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;

import java.util.List;

public interface PointService {
    // 총 4가지 기본 기능 (포인트 조회, 포인트 충전/사용 내역 조회, 충전, 사용) 을 구현
    // 1. 포인트 조회
    UserPoint getUserPoint(long id);
    // 2. 포인트 충전/이용 내역 조회
    List<PointHistory> getUserPointHistory(long id);
    // 3. 포인트 충전
    UserPoint addUserPoint(long id, long amount);
    // 4. 포인트 사용
    UserPoint useUserPoint(long id, long amount);
}
