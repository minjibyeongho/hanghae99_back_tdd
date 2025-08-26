package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.Service.PointService;
import io.hhplus.tdd.point.Service.PointServiceImpl;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointTest {

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;

    @InjectMocks
    PointServiceImpl pointServiceImpl;

    @DisplayName("포인트 조회 테스트")
    @Test
    void givenUserId_whenGetPoint_thenPoint300(){
        // given
        // 1L 유저 포인트 조회 시 300L set
        UserPoint userPointInit = new UserPoint(1L, 300L, System.currentTimeMillis());
        when(userPointTable.selectById(1L))
                .thenReturn(userPointInit);

        // when
        UserPoint result = pointServiceImpl.getUserPoint(1L);

        // then
        assertEquals(300L, result.point());
    }

    @DisplayName("포인트 충전/사용 내역 조회")
    @Test
    void givenUserId_whenGetHistoryList_thenUserPointHistoryList(){

        // given
        // charge 1번, 1000L
        // use 1번, 300L
        long userId = 1L;
        PointHistory pointHistoryCharge = new PointHistory(1L, 1L, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory pointHistoryUse = new PointHistory(1L, 1L, 300L, TransactionType.USE, System.currentTimeMillis());

        List<PointHistory> pointHistoryList = List.of(pointHistoryCharge, pointHistoryUse);

        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(pointHistoryList);

        // when
        List<PointHistory> result = pointServiceImpl.getUserPointHistory(userId);

        // then
        assertThat(result)
                .extracting(PointHistory::id, PointHistory::userId, PointHistory::amount, PointHistory::type)
                .containsExactly(
                        tuple(1L, 1L, 1000L, TransactionType.CHARGE),
                        tuple(1L, 1L, 300L, TransactionType.USE)
                );
    }

    @DisplayName("포인트 충전(잔여금 존재)")
    @Test
    void givenUserIdAmount_whenBalanceCharge_thenAddPoint5000(){
        // given
        UserPoint userPointInit = new UserPoint(1L, 1000L, System.currentTimeMillis());
        long CHARGE_AMOUNT = 5000L;
        UserPoint userPointAfter = new UserPoint(1L, 6000L, System.currentTimeMillis());
        // PointHistory pointHistoryCharge = new PointHistory(1L, 1L, CHARGE_AMOUNT, TransactionType.CHARGE, System.currentTimeMillis());

        // 유저 1L, 잔금 1000L로 설정
        when(userPointTable.selectById(1L)).thenReturn(userPointInit);
        when(userPointTable.insertOrUpdate(1L, 1000L+CHARGE_AMOUNT)).thenReturn(userPointAfter);
        //when(pointHistoryTable.insert(1L, CHARGE_AMOUNT, TransactionType.CHARGE,System.currentTimeMillis())).thenReturn(pointHistoryCharge);

        // when
        UserPoint result = pointServiceImpl.addUserPoint(1L, CHARGE_AMOUNT);

        // then
        assertEquals(6000L, result.point());
        assertEquals(1L, result.id());

        // ?? 히스토리도 검증해야할꺼 같은데.. 방법을 모르겠음;;
    }

    @DisplayName("포인트 충전(잔여금 없을때)")
    @Test
    void givenUserIdAmount_whenNotBalanceCharge_thenAddPoint5000(){
        // given

        UserPoint userPointInit = UserPoint.empty(1L);
        long CHARGE_AMOUNT = 5000L;
        UserPoint userPointAfter = new UserPoint(1L, 5000L, System.currentTimeMillis());
        // PointHistory pointHistoryCharge = new PointHistory(1L, 1L, CHARGE_AMOUNT, TransactionType.CHARGE, System.currentTimeMillis());

        // 유저 1L, 잔금 1000L로 설정
        when(userPointTable.selectById(1L)).thenReturn(userPointInit);
        when(userPointTable.insertOrUpdate(1L, 0L + CHARGE_AMOUNT)).thenReturn(userPointAfter);
        //when(pointHistoryTable.insert(1L, CHARGE_AMOUNT, TransactionType.CHARGE,System.currentTimeMillis())).thenReturn(pointHistoryCharge);

        // when
        UserPoint result = pointServiceImpl.addUserPoint(1L, CHARGE_AMOUNT);

        // then
        assertEquals(5000L, result.point());
        assertEquals(1L, result.id());

        // ?? 히스토리도 검증해야할꺼 같은데.. 방법을 모르겠음;;
    }

    @DisplayName("포인트 사용(잔여금 존재)")
    @Test
    void givenUserIdAmount_whenBalanceUse_thenUsePoint300(){
        // given
        UserPoint userPointInit = new UserPoint(1L, 1000L, System.currentTimeMillis());
        long USE_AMOUNT = 300L;
        UserPoint userPointAfter = new UserPoint(1L, 700L, System.currentTimeMillis());
        // PointHistory pointHistoryUse = new PointHistory(1L, 1L, 300L, TransactionType.USE, System.currentTimeMillis());

        // 유저 1L, 잔금 1000L로 설정
        when(userPointTable.selectById(1L)).thenReturn(userPointInit);
        when(userPointTable.insertOrUpdate(1L, 1000L-USE_AMOUNT)).thenReturn(userPointAfter);
        // when(pointHistoryTable.insert(1L, USE_AMOUNT, TransactionType.USE,System.currentTimeMillis())).thenReturn(pointHistoryUse);

        // when
        UserPoint result = pointServiceImpl.useUserPoint(1L, 300L);

        // then
        assertEquals(700L, result.point());
        assertEquals(1L, result.id());

        // ?? 히스토리도 검증해야할꺼 같은데.. 방법을 모르겠음;;
    }

    @DisplayName("포인트 사용(잔여금 없을때) - 에러")
    @Test
    void givenUserIdAmount_whenNotBalanceUse_thenUsePoint300(){
        // given
        // 유저 1L, 잔여금: 0, 사용금액 300L
        long UserId = 1L;
        long USE_AMOUNT = 300L;
        UserPoint userPointInit = UserPoint.empty(UserId);

        when(userPointTable.selectById(UserId)).thenReturn(userPointInit);

        // when & then
        assertThrows(RuntimeException.class,
                () -> pointServiceImpl.useUserPoint(UserId, USE_AMOUNT)
        );
    }
}
