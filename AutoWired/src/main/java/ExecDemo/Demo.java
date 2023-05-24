package ExecDemo;

import AutoWiredPackage.AutoWired;
import enityDemo.User;

public class Demo {

    @AutoWired
    public User user;


    public User getUser() {
        return this.user;
    }
}
