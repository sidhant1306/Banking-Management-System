
import java.sql.*;
import java.util.Scanner;

public class bank_system {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/bank_system";
    private static final String username = "root";
    private static final String password = "Sonam@1108";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Scanner sc = new Scanner(System.in);

            // asking the user if he is a new user or an old user :
            System.out.print("1. Sign in (New User)\t\t");
            System.out.println("2. Log In");

            int login_option = sc.nextInt();


            // if the user is new :
            if (login_option == 1) {
                createUser(connection, sc);
            } else if (login_option == 2) {
                System.out.println("Enter your user_id : ");
                int user_id = sc.nextInt();
                boolean credentials = userLogin(connection, sc, user_id);
                if (credentials) {
                    print_userInfo(connection, user_id);
                    while (true) {
                        System.out.print("1. Check Balance\t\t");
                        System.out.print("2. Deposit Money\t\t");
                        System.out.println("3. Withdraw Money\t\t");
                        System.out.print("4. Transfer Money\t\t");
                        System.out.println("5. Transaction History\t\t");
                        System.out.println("6. EXIT");

                        int user_operation = sc.nextInt();

                        switch (user_operation) {
                            case 1:
                                print_balance(connection, user_id);
                                break;
                            case 2:
                                deposit_money(connection, sc, user_id);
                                break;
                            case 3:
                                withdraw(connection, sc, user_id);
                                break;
                            case 4:
                                money_transfer(connection, sc, user_id);
                                break;
                            case 5:
                                transaction_history(connection, user_id);
                                break;
                            case 6:
                                sc.close();
                                connection.close();
                                return;


                            default:
                                System.out.println("Invalid input, please try again");

                        }
                    }
                }else {
                    return;
                }

            } else {
                System.out.println("Please enter a valid input");
                return;
            }

            sc.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    // FUNCTION TO CREATE A NEW USER:
    public static void createUser(Connection connection, Scanner sc) {
        try {
            sc.nextLine();
            while (true) {
                String insertUsers = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
                PreparedStatement insertPrep = connection.prepareStatement(insertUsers);
                System.out.println("Enter your name : ");
                String name = sc.nextLine();

                System.out.println("Enter your email : ");
                String email = sc.nextLine();

                System.out.println("Set a new password : ");
                String password = sc.nextLine();

                insertPrep.setString(1, name);
                insertPrep.setString(2, email);
                insertPrep.setString(3, password);
                insertPrep.executeUpdate();

                // Fetch the user_id of this newly created user
                String getUserId = "SELECT user_id FROM users WHERE email = ?";
                PreparedStatement getUserIdPrep = connection.prepareStatement(getUserId);
                getUserIdPrep.setString(1, email);
                ResultSet rs = getUserIdPrep.executeQuery();

                if (rs.next()) {
                    int newUserId = rs.getInt("user_id");

                    // Create a bank account for this new user
                    String createAccount = "INSERT INTO accounts (user_id, balance) VALUES (?, 0)";
                    PreparedStatement accPrep = connection.prepareStatement(createAccount);
                    accPrep.setInt(1, newUserId);
                    accPrep.executeUpdate();

                    System.out.println("User created successfully with user_id: " + newUserId);
                    System.out.println("Bank account created successfully for user_id: " + newUserId);
                    accPrep.close();    // closing the prepared statement instance
                }
                rs.close();
                getUserIdPrep.close();
                insertPrep.close();     // closing the resource at each iteration
                System.out.println("Do you want to add a new user? : Y/N");
                String choice = sc.next();
                sc.nextLine();
                if (choice.equalsIgnoreCase("N")) break;

            }




        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    // for login of a user :

    public static boolean userLogin(Connection connection, Scanner sc, int user_id) {
        System.out.println("Please enter your password : ");
        String password = sc.next();

        if(isValid(connection, user_id, password)){
            return true;
        }
        System.out.println("Username OR Password is Wrong! Please try again.");
        return false;
    }

    // check if the login info is valid :

    public static boolean isValid(Connection connection, int user_id, String password) {
        try {
            String valid_user = "SELECT password FROM users WHERE user_id = ? ";
            PreparedStatement validPrep = connection.prepareStatement(valid_user);
            validPrep.setInt(1, user_id);
            ResultSet pass = validPrep.executeQuery();

            if (pass.next()) {
                String correct_password = pass.getString("password");   // this is the correct password from the database
                return correct_password.equals(password);
            }
            pass.close();
            validPrep.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    // check current balance :

    public static double check_balance(Connection connection, int user_id) {
        try {
            String check_balance = "SELECT balance FROM accounts WHERE user_id = ? ";
            PreparedStatement check_prep = connection.prepareStatement(check_balance);

            check_prep.setInt(1, user_id);
            ResultSet current_balance = check_prep.executeQuery();
            if (current_balance.next()) {
                double bal =  current_balance.getDouble("balance");
                check_prep.close();
                current_balance.close();
                return bal;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return 0;
    }

    public static void print_balance(Connection connection, int user_id) {
        try {
            String check_balance = "SELECT balance FROM accounts WHERE user_id = ? ";
            PreparedStatement check_prep = connection.prepareStatement(check_balance);

            check_prep.setInt(1, user_id);
            ResultSet current_balance = check_prep.executeQuery();
            if (current_balance.next()) {
                System.out.println("Your current bank balance is : " + current_balance.getDouble("balance"));
                current_balance.close();
                check_prep.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // to deposit money in the account :

    public static void deposit_money(Connection connection, Scanner sc, int user_id) throws SQLException {
        try {
            connection.setAutoCommit(false);
            String deposit = "UPDATE accounts SET balance = balance + ? WHERE user_id = ?";
            PreparedStatement deposit_prep = connection.prepareStatement(deposit);

            System.out.println("Enter the amount you want to deposit : ");
            double amount = sc.nextDouble();

            deposit_prep.setDouble(1, amount);
            deposit_prep.setInt(2, user_id);

            int deposited = deposit_prep.executeUpdate();

            if (deposited > 0) {
                String insert_transaction = "INSERT INTO transactions(debit_account, credit_account, amount, status, category) VALUES(?, ? ,?, ?, ?)";
                // inserting the transaction into the transaction table :
                PreparedStatement add_transaction = connection.prepareStatement(insert_transaction);
                // using the function to find the account number of the user:
                int user_account_number = find_acc_number(connection, user_id);
                add_transaction.setInt(1, 0);
                add_transaction.setInt(2, user_account_number);
                add_transaction.setDouble(3, amount);
                add_transaction.setString(4, "Successful");
                add_transaction.setString(5, "deposit");

                add_transaction.executeUpdate();
                System.out.println("Amount : " + amount + " deposited in account of user : " + user_id);
                System.out.println("Current Balance : " + check_balance(connection, user_id));
                add_transaction.close();
                connection.commit();
            } else System.out.println("Amount could not be deposited");
            deposit_prep.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
        }
        finally {
            connection.setAutoCommit(true);
        }
    }

    // to withdraw money:

    public static void withdraw(Connection connection, Scanner sc, int user_id) throws SQLException {
        try {
            connection.setAutoCommit(false);
            System.out.println("Enter the amount that you want to withdraw from your account : ");
            double amount = sc.nextDouble();
            // inserting the transaction in the transaction table :
            String insert_transaction = "INSERT INTO transactions(debit_account, credit_account, amount, status, category) VALUES(?, ? ,?, ?, ?)";
            String withdraw_money = "UPDATE accounts SET balance = balance - ? WHERE user_id = ?";

            if (isSufficient(connection, sc, amount, user_id)) {
                PreparedStatement withdraw_prep = connection.prepareStatement(withdraw_money);
                withdraw_prep.setDouble(1, amount);
                withdraw_prep.setInt(2, user_id);
                withdraw_prep.executeUpdate();
                withdraw_prep.close();

                // inserting the transaction into the transaction table :
                PreparedStatement add_transaction = connection.prepareStatement(insert_transaction);
                // using the function to find the account number of the user:
                int user_account_number = find_acc_number(connection, user_id);
                add_transaction.setInt(1, user_account_number);
                add_transaction.setInt(2, 0);
                add_transaction.setDouble(3, amount);
                add_transaction.setString(4, "Successful");
                add_transaction.setString(5, "withdrawal");
                add_transaction.executeUpdate();
                System.out.println("Withdrawal of " +amount + " was successful");
                System.out.println("Current Balance : " + check_balance(connection, user_id));
                add_transaction.close();
                connection.commit();
            } else {
                System.out.println("Insufficient balance. Withdrawal failed");
                connection.rollback();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
    }
    // Helper function to find the account number :
    public static int find_acc_number(Connection connection, int user_id){
        try {
            String acc_number = "SELECT account_number FROM accounts WHERE user_id = ?";
            PreparedStatement account_number = connection.prepareStatement(acc_number);
            account_number.setInt(1, user_id);
            ResultSet number = account_number.executeQuery();
            if(number.next()){
                return number.getInt("account_number");
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

        return 0;
    }
    // does the user have sufficient money in the account :

    public static boolean isSufficient(Connection connection, Scanner sc, double amount, int user_id) {
        double curr_balance = check_balance(connection, user_id);   // using the check_balance function to get the value of current balance
        return curr_balance >= amount;
    }

    // to transfer money:

    public static void money_transfer(Connection connection, Scanner sc, int debit_user_id) throws SQLException {
        try {

            System.out.println("Enter the amount you want to transfer : ");
            double amount = sc.nextDouble();

            System.out.println("Enter the user_id you want to send money to : ");
            int credit_user_id = sc.nextInt();

            connection.setAutoCommit(false);
            String debit_query = "UPDATE accounts SET balance = balance - ? WHERE user_id = ? ";
            String credit_query = "UPDATE accounts SET balance = balance + ? WHERE user_id = ? ";

            String debit_account = "SELECT account_number FROM accounts WHERE user_id = ?";     // acc number from which the money is debited(for transaction)
            String credit_account = "SELECT account_number FROM accounts WHERE user_id = ?";    // acc number in which the money is credited (for transaction)
            String update_transaction = "INSERT INTO transactions(debit_account, credit_account, amount, status,category) VALUES(?, ? ,?, ?, ?) ";

            PreparedStatement debit_prep = connection.prepareStatement(debit_query);
            PreparedStatement credit_prep = connection.prepareStatement(credit_query);


            if (isSufficient(connection, sc, amount, debit_user_id)) {
                debit_prep.setDouble(1, amount);
                debit_prep.setInt(2, debit_user_id);
                credit_prep.setDouble(1, amount);
                credit_prep.setInt(2, credit_user_id);


                int debitUpdated = debit_prep.executeUpdate();
                int creditUpdated = credit_prep.executeUpdate();
                if (creditUpdated == 0 || debitUpdated == 0) {
                    System.out.println("Invalid recipient. Transfer cancelled.");
                    connection.rollback();
                    return;
                }
                System.out.println("Money transferred successfully");
                print_balance(connection, debit_user_id);

                // update of transaction table :
                // FIRSTLY FIND THE ACCOUNT NUMBER FROM WHICH THE MONEY IS DEBITED.
                // WE CAN NOT JUST DIRECTLY FIND DEBIT AND CREDIT ACCOUNT NUMBER USING TWO RESULT SETS, BECAUSE USE OF ONLY ONE RESULT SET IS ALLOWED...
                // AT A TIME IN A CONNECTION
                //SO WE NEED TO CLOSE THE DEBIT RESULT SET BEFORE CREATING THE SAME TO FIND THE CREDIT ACCOUNT NUMBER.
                int debit_account_number = 0;   // declaring it here to use it globally, value is updates inside the resultSet
                PreparedStatement debited_account = connection.prepareStatement(debit_account);
                debited_account.setInt(1, debit_user_id);
                ResultSet debit_set = debited_account.executeQuery();
                if(debit_set.next()){
                    debit_account_number = debit_set.getInt("account_number");
                }
                debit_set.close();
                debited_account.close();

                // FINDING THE CREDITED ACCOUNT NUMBER:
                int credit_account_number = 0;
                PreparedStatement credited_account = connection.prepareStatement(credit_account);
                credited_account.setInt(1, credit_user_id);
                ResultSet credit_set = credited_account.executeQuery();
                if (credit_set.next()) {
                    credit_account_number = credit_set.getInt("account_number");
                }
                credit_set.close();
                credited_account.close();

                // now queries to update the details in the transaction table :

                PreparedStatement insert_transaction_prep = connection.prepareStatement(update_transaction);
                insert_transaction_prep.setInt(1, debit_account_number);
                insert_transaction_prep.setInt(2, credit_account_number);
                insert_transaction_prep.setDouble(3, amount);
                insert_transaction_prep.setString(4, "successful");
                insert_transaction_prep.setString(5, "Transfer");
                insert_transaction_prep.executeUpdate();
                connection.commit();

            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }

    }

    // transaction history :

    public static void transaction_history(Connection connection, int user_id) {
        try {
            String get_account = "SELECT account_number from accounts WHERE user_id = ? ";
            PreparedStatement acc_number = connection.prepareStatement(get_account);
            acc_number.setInt(1, user_id);  // assigning the user id whose account we want 1to get
            ResultSet acc_set = acc_number.executeQuery();      // executing the result set

            if (acc_set.next()) {
                int user_acc_number = acc_set.getInt("account_number");     // storing the value of the result(acc number) in the variable
                String show_history = "SELECT * FROM transactions WHERE debit_account = ? OR credit_account = ? ";       // to fetch the transactions via using the user account number
                PreparedStatement history_prep = connection.prepareStatement(show_history);
                history_prep.setInt(1, user_acc_number);    // setting the value of debit_account
                history_prep.setInt(2, user_acc_number);    // setting the value of credit_account
                ResultSet history = history_prep.executeQuery();
                boolean found = false;
                while (history.next()) {
                    found = true;
                    System.out.println("-----------------------------");
                    System.out.println("Transaction ID: " + history.getInt("transaction_id"));
                    System.out.println("Debit Account : " + history.getInt("debit_account"));
                    System.out.println("Credit Account: " + history.getInt("credit_account"));
                    System.out.println("Amount        : " + history.getDouble("amount"));
                    System.out.println("Status        : " + history.getString("status"));
                    System.out.println("Category    : " + history.getString("category"));
                    System.out.println("Created At    : " + history.getTimestamp("created_at"));

                    System.out.println("-----------------------------");
                }
                if (!found){
                    System.out.println("No transactions found for this account.");
                }
                history.close();
                history_prep.close();
                acc_set.close();
                acc_number.close();
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void print_userInfo(Connection connection, int user_id) {
        try {
            String get_data = "SELECT name,email FROM users WHERE user_id = ?";
            PreparedStatement user_data = connection.prepareStatement(get_data);
            user_data.setInt(1, user_id);
            ResultSet data = user_data.executeQuery();
            while(data.next()){
                String name = data.getString("name");
                String email = data.getString("email");
                System.out.println("Name : " + name);
                System.out.println("email : " + email);
                System.out.println();
            }
            data.close();
            user_data.close();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }



    }




}
