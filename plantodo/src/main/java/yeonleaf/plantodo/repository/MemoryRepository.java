package yeonleaf.plantodo.repository;

import java.util.HashMap;
import java.util.Map;

public abstract class MemoryRepository<T> {

    public Map<Long, T> data = new HashMap<>();
    public abstract T save(T t);

}
