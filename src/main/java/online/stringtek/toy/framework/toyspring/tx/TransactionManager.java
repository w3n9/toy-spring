package online.stringtek.toy.framework.toyspring.tx;

import java.sql.SQLException;

public interface TransactionManager {
    void beginTransaction() throws SQLException;
    void commit() throws SQLException;
    void rollback() throws SQLException;
}
