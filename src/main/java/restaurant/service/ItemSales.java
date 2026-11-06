package restaurant.service;

/**
 * Một dòng trong thống kê món bán chạy.
 *
 * @param itemName tên món
 * @param quantity tổng số lượng đã bán
 * @param revenue tổng doanh thu của món (trước giảm giá)
 */
public record ItemSales(String itemName, int quantity, long revenue) {
}
