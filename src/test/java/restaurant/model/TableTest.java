package restaurant.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TableTest {

    @Test
    @DisplayName("Bàn chuyển giữa trống và có khách")
    void occupyAndRelease() {
        Table t = new Table(3, 4);
        assertTrue(t.isFree());
        assertEquals(3, t.getId());
        t.occupy();
        assertEquals(TableStatus.OCCUPIED, t.getStatus());
        assertThrows(IllegalStateException.class, t::occupy);
        t.release();
        assertTrue(t.isFree());
        assertThrows(IllegalStateException.class, t::release);
    }

    @Test
    @DisplayName("Số bàn và số ghế phải từ 1 trở lên")
    void rejectsInvalidNumbers() {
        assertThrows(IllegalArgumentException.class, () -> new Table(0, 4));
        assertThrows(IllegalArgumentException.class, () -> new Table(1, 0));
    }
}
