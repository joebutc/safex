package app.model;

public class UserAccount extends Account {
    public UserAccount(String username, String password, String email) {
        super(username, password, email);
    }

    public UserAccount(String username, String password, String email, String address) {
        super(username, password, email, address);
    }
}
