package labs.mybatis.dao.test1;

import java.util.List;
import labs.mybatis.model.test1.TestP;
import labs.mybatis.model.test1.TestPExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface TestPMapper {
    int deleteByExample(TestPExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TestP record);

    int insertSelective(TestP record);

    List<TestP> selectByExampleWithRowbounds(TestPExample example, RowBounds rowBounds);

    List<TestP> selectByExample(TestPExample example);

    TestP selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TestP record, @Param("example") TestPExample example);

    int updateByExample(@Param("record") TestP record, @Param("example") TestPExample example);

    int updateByPrimaryKeySelective(TestP record);

    int updateByPrimaryKey(TestP record);
}