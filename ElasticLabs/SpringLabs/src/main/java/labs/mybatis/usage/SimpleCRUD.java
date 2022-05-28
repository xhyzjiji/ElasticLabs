package labs.mybatis.usage;

import Serializer.JSONUtil;
import java.util.List;
import javax.annotation.Resource;
import labs.mybatis.dao.test1.TestPMapper;
import labs.mybatis.dao.test2.TestP2Mapper;
import labs.mybatis.model.test1.TestP;
import labs.mybatis.model.test1.TestPExample;
import labs.mybatis.model.test2.TestP2;
import labs.mybatis.model.test2.TestP2Example;
import org.springframework.stereotype.Service;

@Service
public class SimpleCRUD {

    @Resource
    private TestPMapper testPMapper;
    @Resource
    private TestP2Mapper testP2Mapper;

    public List<TestP> getTestObjects(int maxId) {
        TestPExample testPExample = new TestPExample();
        testPExample.createCriteria().andIdLessThan(Integer.toUnsignedLong(maxId));
        List<TestP> res = testPMapper.selectByExample(testPExample);
        System.out.println(JSONUtil.toJsonStringSilent(res, false));
        return res;
    }

    public List<TestP2> getTest2Objects(int maxId) {
        TestP2Example testP2Example = new TestP2Example();
        testP2Example.createCriteria().andIdLessThan(Integer.toUnsignedLong(maxId));
        List<TestP2> res = testP2Mapper.selectByExample(testP2Example);
        System.out.println(JSONUtil.toJsonStringSilent(res, false));
        return res;
    }

}
