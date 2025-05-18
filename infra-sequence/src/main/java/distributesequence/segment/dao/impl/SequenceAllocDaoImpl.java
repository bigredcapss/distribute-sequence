package distributesequence.segment.dao.impl;

import com.peanut.infra.distributesequence.segment.dao.SequenceAllocDao;
import com.peanut.infra.distributesequence.segment.dao.SequenceAllocMapper;
import com.peanut.infra.distributesequence.segment.model.SequenceAlloc;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;

public class SequenceAllocDaoImpl implements SequenceAllocDao {

    SqlSessionFactory sqlSessionFactory;

    public SequenceAllocDaoImpl(DataSource dataSource) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(SequenceAllocMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    public List<SequenceAlloc> getAllSequenceAllocs() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList("com.soul.infra.sequence.segment.dao.SequenceAllocMapper.getAllSequenceAllocs");
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public SequenceAlloc updateMaxIdAndGetSequenceAlloc(String tag) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession.update("com.soul.infra.sequence.segment.dao.SequenceAllocMapper.updateMaxId", tag);
            SequenceAlloc result = sqlSession.selectOne("com.soul.infra.sequence.segment.dao.SequenceAllocMapper.getSequenceAlloc", tag);
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public SequenceAlloc updateMaxIdByCustomStepAndGetSequenceAlloc(SequenceAlloc sequenceAlloc) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession.update("com.soul.infra.sequence.segment.dao.SequenceAllocMapper.updateMaxIdByCustomStep", sequenceAlloc);
            SequenceAlloc result = sqlSession.selectOne("com.soul.infra.sequence.segment.dao.SequenceAllocMapper.getSequenceAlloc", sequenceAlloc.getKey());
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<String> getAllTags() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList("com.soul.infra.sequence.segment.dao.SequenceAllocMapper.getAllTags");
        } finally {
            sqlSession.close();
        }
    }
}