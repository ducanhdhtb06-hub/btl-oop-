package restaurant.model;

/**
 * Đối tượng có mã định danh. {@link restaurant.repository.Repository} dùng giới hạn kiểu này
 * để biết lấy khóa từ đâu.
 *
 * @param <ID> kiểu của mã
 */
public interface Identifiable<ID> {

    /**
     * Trả về mã định danh.
     *
     * @return mã của đối tượng
     */
    ID getId();
}
