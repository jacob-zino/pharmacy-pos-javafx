package pharmacy_pos;

/**
 * Command-line helper to add a login: gradlew createUser --args="username password ADMIN|CASHIER"
 */
public class CreateUser {

    public static void main(String[] args) throws Exception {
        if (args.length != 3 || !(args[2].equalsIgnoreCase("ADMIN") || args[2].equalsIgnoreCase("CASHIER"))) {
            System.err.println("Usage: createUser <username> <password> <ADMIN|CASHIER>");
            System.exit(1);
        }
        new UserDAO().createUser(args[0], args[1], args[2].toUpperCase());
        System.out.println("User '" + args[0] + "' created with role " + args[2].toUpperCase());
    }
}
