package AutoWiredPackage;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * 自动装配实现代码
 */
public class AutoWiredDemo {

    private HashMap<Object, Object> context;

    public AutoWiredDemo(HashMap<Object, Object> context) {
        this.context = context;
    }

    public HashMap<Object, Object> checkHaveAutoWiredAnnotion(Object bean) {
        Class<?> beanClass = bean.getClass();
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(AutoWired.class)) {

                Class<?> type = declaredField.getType();
                Object filedValue = context.get(type);

                try {
                    declaredField.setAccessible(true);
                    declaredField.set(bean, filedValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return this.context;

    }

}
