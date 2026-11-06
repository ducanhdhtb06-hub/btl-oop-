package restaurant.model;

/**
 * Một dòng trên hóa đơn.
 *
 * @param itemName tên món
 * @param quantity số lượng
 * @param unitPrice đơn giá đã chốt lúc gọi món
 * @param lineTotal thành tiền
 */
public record BillLine(String itemName, int quantity, long unitPrice, long lineTotal) {
}
