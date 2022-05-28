package labs.mybatis.dao.test2;

import java.util.List;
import labs.mybatis.model.test2.TestP2;
import labs.mybatis.model.test2.TestP2Example;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface TestP2Mapper {
    int deleteByExample(TestP2Example example);

    int deleteByPrimaryKey(Long id);

    int insert(TestP2 record);

    int insertSelective(TestP2 record);

    List<TestP2> selectByExampleWithRowbounds(TestP2Example example, RowBounds rowBounds);

    List<TestP2> selectByExample(TestP2Example example);

    TestP2 selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TestP2 record, @Param("example") TestP2Example example);

    int updateByExample(@Param("record") TestP2 record, @Param("example") TestP2Example example);

    int updateByPrimaryKeySelective(TestP2 record);

    int updateByPrimaryKey(TestP2 record);
}