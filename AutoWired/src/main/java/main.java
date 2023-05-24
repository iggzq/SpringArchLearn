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
        //自动装配
        AutoWiredDemo autoWiredDemo = new AutoWiredDemo(hashMap);
        Demo demo1 = (Demo) hashMap.get(Demo.class);
        System.out.println(demo1.getUser().getName());
    }
}
