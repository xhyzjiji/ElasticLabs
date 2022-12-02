package labs.mybatis.controller;

import Serializer.JSONUtil;
import labs.mybatis.usage.SimpleCRUD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private SimpleCRUD simpleCRUD;

    @RequestMapping("/hello")
    public String hello() {
        return "Hello world!";
    }

    /**
     * localhost:/test
     * @return
     */
    @RequestMapping("/test")
    public String test() {
        return JSONUtil.toJsonStringSilent(simpleCRUD.getTestObjects(10), false);
    }

    @RequestMapping("/test2")
    public String test2() {
        return JSONUtil.toJsonStringSilent(simpleCRUD.getTest2Objects(10), false);
    }
}
