import AutoWiredPackage.AutoWiredDemo;
import ExecDemo.Demo;
import enityDemo.User;

import java.util.HashMap;

public class main {
    public static void main(String[] args) throws NoSuchFieldException {
        //当作context容器
        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        //将User和Demo存储在容器中,作为Bean
        Demo demo = new Demo();
        User user = new User("hello,AutoWired");
        hashMap.put(Demo.class, demo);
        hashMap.put(User.class, user);
        Demo demo1 = (Demo) hashMap.get(Demo.class);
        //未装配前
        try {
            System.out.println("未装配前:" + demo1.getUser().getName());
        } catch (NullPointerException e) {
            System.out.println("未装配前:demo1.getUser().getName()对象为空");
        }

        //自动装配
        AutoWiredDemo autoWiredDemo = new AutoWiredDemo(hashMap);
        autoWiredDemo.checkHaveAutoWiredAnnotion(demo);

        //测试结果
        System.out.println("装配后:demo1.getUser().getName()值为"+demo1.getUser().getName());
    }
}
