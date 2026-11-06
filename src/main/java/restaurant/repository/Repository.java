package restaurant.repository;

import java.util.List;
import java.util.Optional;
import restaurant.model.Identifiable;

/**
 * Nơi lưu trữ đối tượng có mã. Các service chỉ phụ thuộc vào interface này,
 * nên đổi từ lưu trong bộ nhớ sang lưu file không phải sửa service (nguyên tắc D).
 *
 * @param <T> kiểu đối tượng, phải có mã ({@link Identifiable})
 * @param <ID> kiểu của mã
 */
public interface Repository<T extends Identifiable<ID>, ID> {

    /**
     * Trả về mọi đối tượng.
     *
     * @return danh sách chỉ đọc
     */
    List<T> findAll();

    /**
     * Tìm theo mã.
     *
     * @param id mã cần tìm
     * @return đối tượng nếu có
     */
    Optional<T> findById(ID id);

    /**
     * Thêm mới, hoặc thay thế đối tượng cùng mã.
     *
     * @param entity đối tượng cần lưu
     */
    void save(T entity);
}
