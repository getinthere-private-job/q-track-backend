package dev.dote.qtrack.dailyproduction;

import dev.dote.qtrack._core.errors.ex.Exception400;
import dev.dote.qtrack.item.Item;
import dev.dote.qtrack.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 생산 데이터 비즈니스 로직 처리
 * - 일별 생산 데이터 조회, 생성, 수정, 삭제 기능
 * - 부품별 일일 생산 수량 관리 및 검증
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DailyProductionService {
    private final DailyProductionRepository dailyProductionRepository;
    private final ItemRepository itemRepository;

    public List<DailyProductionResponse.List> findAll() {
        return dailyProductionRepository.findAllWithItem().stream()
                .map(dp -> new DailyProductionResponse.List(
                        dp.getId(),
                        dp.getItem().getId(),
                        dp.getProductionDate(),
                        dp.getTotalQuantity()))
                .toList();
    }

    public Page<DailyProductionResponse.List> findAll(Pageable pageable, Long itemId, LocalDate startDate, LocalDate endDate) {
        return dailyProductionRepository.findAllWithFilters(pageable, itemId, startDate, endDate)
                .map(dp -> new DailyProductionResponse.List(
                        dp.getId(),
                        dp.getItem().getId(),
                        dp.getProductionDate(),
                        dp.getTotalQuantity()));
    }

    public DailyProductionResponse.Get findById(Long id) {
        DailyProduction dailyProduction = dailyProductionRepository.findByIdWithItem(id)
                .orElseThrow(() -> new Exception400("일별 생산 데이터를 찾을 수 없습니다: " + id));
        return new DailyProductionResponse.Get(
                dailyProduction.getId(),
                dailyProduction.getItem().getId(),
                dailyProduction.getProductionDate(),
                dailyProduction.getTotalQuantity());
    }

    @Transactional
    public DailyProductionResponse.Create create(Long itemId, LocalDate productionDate, Integer totalQuantity) {
        if (totalQuantity == null || totalQuantity < 0) {
            throw new Exception400("총 생산 수량은 0 이상이어야 합니다: " + totalQuantity);
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception400("부품을 찾을 수 없습니다: " + itemId));

        if (dailyProductionRepository.existsByItemAndProductionDate(item, productionDate)) {
            throw new Exception400("이미 존재하는 일별 생산 데이터입니다: 부품ID=" + itemId + ", 생산일자=" + productionDate);
        }

        DailyProduction dailyProduction = new DailyProduction(item, productionDate, totalQuantity);
        DailyProduction saved = dailyProductionRepository.save(dailyProduction);
        return new DailyProductionResponse.Create(
                saved.getId(),
                saved.getItem().getId(),
                saved.getProductionDate(),
                saved.getTotalQuantity());
    }

    @Transactional
    public DailyProductionResponse.Update update(Long id, Integer totalQuantity) {
        if (totalQuantity == null || totalQuantity < 0) {
            throw new Exception400("총 생산 수량은 0 이상이어야 합니다: " + totalQuantity);
        }

        DailyProduction dailyProduction = dailyProductionRepository.findByIdWithItem(id)
                .orElseThrow(() -> new Exception400("일별 생산 데이터를 찾을 수 없습니다: " + id));

        dailyProduction.update(totalQuantity);
        DailyProduction updated = dailyProductionRepository.save(dailyProduction);
        return new DailyProductionResponse.Update(
                updated.getId(),
                updated.getItem().getId(),
                updated.getProductionDate(),
                updated.getTotalQuantity());
    }

    @Transactional
    public DailyProductionResponse.Delete delete(Long id) {
        DailyProduction dailyProduction = dailyProductionRepository.findById(id)
                .orElseThrow(() -> new Exception400("일별 생산 데이터를 찾을 수 없습니다: " + id));
        dailyProductionRepository.delete(dailyProduction);
        return new DailyProductionResponse.Delete(id);
    }
}
