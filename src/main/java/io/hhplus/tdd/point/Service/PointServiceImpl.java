package io.hhplus.tdd.point.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    @Override
    public UserPoint getUserPoint(long id) {
        return userPointTable.selectById(id);
    }

    @Override
    public List<PointHistory> getUserPointHistory(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    @Override
    public UserPoint addUserPoint(long id, long amount) {
        // 1. 충전 전에 id에 잔여 포인트를 조회
        UserPoint userPoint = userPointTable.selectById(id);
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
        // 2. 포인트 충전
        return userPointTable.insertOrUpdate(id, userPoint.point() + amount);
    }

    @Override
    public UserPoint useUserPoint(long id, long amount) {
        // 1. 유저의 포인트 조회
        UserPoint userPoint = userPointTable.selectById(id);
        // 2. 잔여포인트가 0 or 사용할 포인트가 잔여포인트 보다 작을경우 에러 발생
        if(userPoint.point() <= 0) {
            throw new RuntimeException("잔여포인트가 0원입니다.");
        }

        if(amount > userPoint.point()) {
            throw new RuntimeException("잔액 부족");
        }
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        // 3. 포인트 사용
        return userPointTable.insertOrUpdate(id, userPoint.point() - amount);
    }
}
