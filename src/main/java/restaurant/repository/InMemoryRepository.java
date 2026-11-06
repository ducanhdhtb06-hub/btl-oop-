package restaurant.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import restaurant.model.Identifiable;

/**
 * Lưu đối tượng trong bộ nhớ. Dùng {@link LinkedHashMap} để tìm theo mã nhanh và giữ thứ tự thêm vào.
 * Dùng cho bàn, đơn và trong test.
 *
 * @param <T> kiểu đối tượng
 * @param <ID> kiểu của mã
 */
public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {

    private final Map<ID, T> entities = new LinkedHashMap<>();

    @Override
    public List<T> findAll() {
        return List.copyOf(entities.values());
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public void save(T entity) {
        entities.put(entity.getId(), entity);
    }
}
