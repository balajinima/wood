package org.noear.wood.mapper;

import org.noear.wood.*;
import org.noear.wood.ext.Act1;
import org.noear.wood.ext.Act2;
import org.noear.wood.impl.IPageImpl;
import org.noear.wood.utils.RunUtils;
import org.noear.wood.utils.StringUtils;
import org.noear.wood.wrap.Property;
import org.noear.wood.wrap.PropertyWrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by noear on 19-12-11.
 */
public class BaseMapperWrap<T> implements BaseMapper<T> {
    private DbContext _db;
    private BaseEntityWrap _table;
    private String _tabelName;

    private Class<?> _entityType;
    /**
     * 实体类型
     * */
    protected Class<?> entityType() {
        return _entityType;
    }


    public BaseMapperWrap(DbContext db, Class<?> entityType, String tableName) {
        _db = db;
        _entityType = entityType; //给 BaseEntityWrap 用的
        _table = BaseEntityWrap.get(this);

        if (StringUtils.isEmpty(tableName)) {
            _tabelName = _table.tableName;
        } else {
            _tabelName = tableName;
        }
    }

    public BaseMapperWrap(DbContext db, BaseMapper<T> baseMapper) {
        _db = db;
        _table = BaseEntityWrap.get(baseMapper);
        _tabelName = _table.tableName;
    }

    @Override
    public DbContext db() {
        return _db;
    }
    /**
     * 表名
     * */
    @Deprecated
    public String tableName() {
        return _tabelName;
    }
    /**
     * 主键
     * */
    @Override
    public String tablePk() {
        return _table.pkName;
    }

    /**
     * 实体类
     * */
    @Override
    public Class<?> entityClz() {
        return _table.entityClz;
    }


    @Override
    public Long insert(T entity, boolean excludeNull) {
        DataItem data = new DataItem();

        if (excludeNull) {
            data.setEntityIf(entity, (k, v) -> v != null);
        } else {
            data.setEntityIf(entity, (k, v) -> true);
        }

        return RunUtils.call(()
                -> getQr().insert(data));
    }

    @Override
    public void insertList(List<T> list) {
        List<DataItem> list2 = new ArrayList<>();
        for (T d : list) {
            list2.add(new DataItem().setEntityIf(d, (k, v) -> true));
        }

        RunUtils.call(()
                -> getQr().insertList(list2));
    }

    @Override
    public Integer deleteById(Object id) {
        return RunUtils.call(()
                -> getQr().whereEq(tablePk(), id).delete());
    }

    @Override
    public Integer deleteByIds(Iterable idList) {
        return RunUtils.call(()
                -> getQr().whereIn(tablePk(), idList).delete());
    }

    @Override
    public Integer deleteByMap(Map<String, Object> columnMap) {
        return RunUtils.call(()
                -> getQr().whereMap(columnMap).delete());
    }

    @Override
    public Integer delete(Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> {
            return getQr(c).delete();
        });
    }

    @Override
    public Integer updateById(T entity, boolean excludeNull) {
        DataItem data = new DataItem();

        if (excludeNull) {
            data.setEntityIf(entity, (k, v) -> v != null);
        } else {
            data.setEntityIf(entity, (k, v) -> true);
        }

        Object id = data.get(tablePk());

        return RunUtils.call(()
                -> getQr().whereEq(tablePk(), id).update(data));
    }

    @Override
    public Integer update(T entity, boolean excludeNull, Act1<MapperWhereQ> c) {
        DataItem data = new DataItem();

        if (excludeNull) {
            data.setEntityIf(entity, (k, v) -> v != null);
        } else {
            data.setEntityIf(entity, (k, v) -> true);
        }

        return RunUtils.call(() -> {
            return getQr(c).update(data);
        });
    }

    @Override
    public int[] updateList(List<T> list, Act2<T, DataItem> dataBuilder, Property<T, ?>... conditionFields) {
        if (conditionFields.length == 0) {
            throw new RuntimeException("Please enter constraints");
        }

        StringBuilder buf = new StringBuilder();

        for (Property<T, ?> p : conditionFields) {
            buf.append(PropertyWrap.get(p).name).append(",");
        }

        buf.setLength(buf.length() - 1);

        return RunUtils.call(()
                -> getQr().updateList(list, dataBuilder, buf.toString()));
    }

    @Override
    public Long upsert(T entity, boolean excludeNull) {
        DataItem data = new DataItem();

        if (excludeNull) {
            data.setEntityIf(entity, (k, v) -> v != null);
        } else {
            data.setEntityIf(entity, (k, v) -> true);
        }

        Object id = data.get(tablePk());

        if (id == null) {
            return RunUtils.call(() -> getQr().insert(data));
        } else {
            return RunUtils.call(() -> getQr().upsertBy(data, tablePk()));
        }
    }

    @Override
    public Long upsertBy(T entity, boolean excludeNull, String conditionFields) {
        DataItem data = new DataItem();

        if (excludeNull) {
            data.setEntityIf(entity, (k, v) -> v != null);
        } else {
            data.setEntityIf(entity, (k, v) -> true);
        }

        return RunUtils.call(() -> getQr().upsertBy(data, conditionFields));
    }

    @Override
    public boolean existsById(Object id) {
        return RunUtils.call(()
                -> getQr().whereEq(tablePk(), id).selectExists());
    }

    @Override
    public boolean exists(Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> {
            return getQr(c).selectExists();
        });
    }

    @Override
    public T selectById(Object id) {
        Class<T> clz = (Class<T>) entityClz();

        return RunUtils.call(()
                -> getQr().whereEq(tablePk(), id).limit(1).selectItem("*", clz));
    }

    @Override
    public List<T> selectByIds(Iterable idList) {
        Class<T> clz = (Class<T>) entityClz();

        return RunUtils.call(()
                -> getQr().whereIn(tablePk(), idList).selectList("*", clz));
    }

    @Override
    public List<T> selectByMap(Map<String, Object> columnMap) {
        Class<T> clz = (Class<T>) entityClz();

        return RunUtils.call(()
                -> getQr().whereMap(columnMap).selectList("*", clz));
    }

    @Override
    public T selectItem(T entity) {
        Class<T> clz = (Class<T>) entityClz();

        return RunUtils.call(() ->
                getQr().whereEntity(entity).limit(1).selectItem("*", clz));
    }

    @Override
    public T selectItem(Act1<MapperWhereQ> c) {
        Class<T> clz = (Class<T>) entityClz();

        return RunUtils.call(() -> getQr(c).selectItem("*", clz));
    }

    @Override
    public Object selectValue(String column, Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> getQr(c).selectValue(column));
    }

    @Override
    public Map<String, Object> selectMap(Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> getQr(c).selectMap("*"));
    }

    @Override
    public Long selectCount(Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> getQr(c).selectCount());
    }

    @Override
    public List<T> selectList(Act1<MapperWhereQ> c) {
        Class<T> clz = (Class<T>) entityClz();

        return RunUtils.call(() -> getQr(c).selectList("*", clz));
    }

    @Override
    public List<Map<String, Object>> selectMapList(Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> getQr(c).selectMapList("*"));
    }

    @Override
    public List<Object> selectArray(String column, Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> getQr(c).selectArray(column));
    }

    @Override
    public List<T> selectList(int start, int size, Act1<MapperWhereQ> c) {
        Class<T> clz = (Class<T>) entityClz();

        return RunUtils.call(() -> getQr(c).limit(start, size).selectList("*", clz));
    }

    @Override
    public List<Map<String, Object>> selectMapList(int start, int size, Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> getQr(c).limit(start, size).selectMapList("*"));
    }

    @Override
    public List<Object> selectArray(String column, int start, int size, Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> getQr(c).limit(start, size).selectArray(column));
    }


    @Override
    public IPage<T> selectPage(int start, int size, Act1<MapperWhereQ> c) {
        Class<T> clz = (Class<T>) entityClz();

        List<T> list = RunUtils.call(() -> getQr(c).limit(start, size).selectList("*", clz));
        long total = RunUtils.call(() -> getQr(c).selectCount());

        IPageImpl<T> page = new IPageImpl<>(list, total, size);

        return page;
    }

    @Override
    public IPage<Map<String, Object>> selectMapPage(int start, int size, Act1<MapperWhereQ> c) {
        List<Map<String, Object>> list = RunUtils.call(() -> getQr(c).limit(start, size).selectMapList("*"));
        long total = RunUtils.call(() -> getQr(c).selectCount());

        IPageImpl<Map<String, Object>> page = new IPageImpl<>(list, total, size);

        return page;
    }

    @Override
    public List<T> selectTop(int size, Act1<MapperWhereQ> c) {
        Class<T> clz = (Class<T>) entityClz();

        return RunUtils.call(() -> getQr(c).top(size).selectList("*", clz));
    }

    @Override
    public List<Map<String, Object>> selectMapTop(int size, Act1<MapperWhereQ> c) {
        return RunUtils.call(() -> getQr(c).top(size).selectMapList("*"));
    }

    /**
     * 获取查询器
     * */
    protected DbTableQuery getQr() {
        return db().table(tableName());
    }

    /**
     * 获取带条件的查询器
     * */
    protected DbTableQuery getQr(Act1<MapperWhereQ> c) {
        DbTableQuery qr = db().table(tableName());

        if (c != null) {
            c.run(new MapperWhereQ(qr));
        }

        return qr;
    }
}
