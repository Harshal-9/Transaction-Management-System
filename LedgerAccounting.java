/**
 * DEVELOPERS = "Harshal" and "Shreyash" !
 */

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.*;

// Executable class containing main
public class LedgerAccounting
{
    public static String url;
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    public static void main(String[] args) throws Exception 
    {
        // Connecting to Database
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        Class.forName("oracle.jdbc.driver.OracleDriver");
        System.out.println("Driver Loaded");

        DatabaseAccountDetails dbad = new DatabaseAccountDetails();
        while(dbad.isActive()){}


        Connection con = DriverManager.getConnection(url,dbad.getUserId(),dbad.getPassword());
        System.out.println("connection established");
        if(con.isValid(10))
        st = con.createStatement();

        // Create database only once !
        create_Database();

        MainFrame m = new MainFrame(url,con,st,rs);
    }
   
    static void create_Database()
    {
        String sql = "CREATE TABLE Ledger_Accounting " +
        "(cust_Id NUMBER(5), " + 
        " name VARCHAR(25), " + 
        " amt_paid NUMBER(8,2), " + 
        " amt_left NUMBER(8,2), " + 
        " prod_type VARCHAR(25), " +
        " due_date Date, "+
        " PRIMARY KEY(cust_Id))"; 
       try
       {
            st.execute(sql);
            System.out.println("Created Ledger_Accounting database !..."); 
            JOptionPane.showMessageDialog(null,"Created Ledger_Accounting database !...");
       }
       catch(Exception e)
       {
           JOptionPane.showMessageDialog(null,"Data Base already exists on device");
       }
    }
}

// Class for main menu
class MainFrame extends Frame implements ActionListener
{
    Button b1,b2,b3,b4,b5;
    Label l1;
    Panel p1,p2,p3;
    Image img;
    int no;
    
    public static String url;
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    AddTransaction at;
    UpdateCustomerRecord ucr;
    FetchPendingRecord fpr;

    public MainFrame(String a,Connection b,Statement c,ResultSet d) throws Exception
    {
        super("Ledger Accounting");
        no = 0;
        url = a; con = b; st = c; rs = d;

        b1 = new Button("Add customer record");
        b2 = new Button("Display all records");
        b3 = new Button("Update existing record");
        b4 = new Button("Show records with pending amt");
        b5 = new Button("Show records with no due amt");

        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
        b4.addActionListener(this);
        b5.addActionListener(this);

        l1 = new Label("Ledger Accounting");

        l1.setFont(new Font("Verdana", Font.PLAIN, 24));  
        b1.setFont(new Font("Verdana", Font.PLAIN, 15));  
        b2.setFont(new Font("Verdana", Font.PLAIN, 15));  
        b3.setFont(new Font("Verdana", Font.PLAIN, 15));  
        b4.setFont(new Font("Verdana", Font.PLAIN, 15));  
        b5.setFont(new Font("Verdana", Font.PLAIN, 15));  

        p1 = new Panel();
        p2 = new Panel();
        p3 = new Panel();

        p1.setLayout(new FlowLayout(FlowLayout.CENTER,10,30));
        p1.add(l1);

        p2.setLayout(new GridLayout(5,1,5,40));
        p2.add(b1);
        p2.add(b2);
        p2.add(b3);
        p2.add(b4);
        p2.add(b5);


        add(p1,BorderLayout.NORTH);
        add(p2,BorderLayout.WEST);

        img =(Image)ImageIO.read(new File("Database.jpeg"));
        p3.setLayout(new BorderLayout());
        p3.repaint();

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        setVisible(true);
        setSize(700,700);
        setResizable(false);
    }    

    public void actionPerformed(ActionEvent e)
    {
        Button b = (Button)e.getSource();

        if(b==b1)
        {
            try
            {
                at = new AddTransaction(url,con,st,rs);
            }catch(Exception e1){}
        }
        if(b==b2)
        {
            try
            {
                show_complete_sheet();
            }catch(Exception e2){}
        } 
        if(b==b3)
        {
            try
            {
                ucr = new UpdateCustomerRecord(url,con,st,rs);
            }catch(Exception e4){}
        } 
        if(b==b4)
        {
            try
            {
                fpr = new FetchPendingRecord(url,con,st,rs);
            }catch(Exception e3){}
        } 
        if(b==b5)
        {
            try
            {
                fetchAllclearRecord();
            }catch(Exception e3){};
        }

    }

    public void paint(Graphics g)
    {
        g.drawImage(img,20,20, this);
    }

    static void show_complete_sheet() throws Exception
    {
        String query = "Select * From ledger_Accounting";
        rs = st.executeQuery(query);

        String output = "";
        ResultSetMetaData rsmd = rs.getMetaData(); 
        output += String.format("\n%1$-8s %2$-20s %3$-10s %4$-10s %5$-20s %6$-10s",rsmd.getColumnName(1),rsmd.getColumnName(2),rsmd.getColumnName(3),rsmd.getColumnName(4),rsmd.getColumnName(5),rsmd.getColumnName(6));

        while(rs.next())
        {
            output += String.format("\n%1$-8s %2$-20s %3$-10s %4$-10s %5$-20s %6$-10s",rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6));
        }
        System.out.println(output);
        JOptionPane.showMessageDialog(null,output);
    }

    static void fetchAllclearRecord() throws Exception
    {
        String sql = "SELECT * FROM Ledger_Accounting WHERE amt_left = 0";
        rs = st.executeQuery(sql);

        String output = "";
        while(rs.next())
        {
            for(int i=1;i<=6;i++)
            {
                output = output + rs.getString(i)+ "    ";
            }
            output += "\n";
        }
        JOptionPane.showMessageDialog(null,output);
    }
}

// Class to fetch selected record
class FetchPendingRecord extends Frame implements ActionListener
{
    public static String url;
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    Button b1;
    Button b2;
    Label l;
    String sql;
    public FetchPendingRecord(String a,Connection b,Statement c,ResultSet d) throws Exception
    {
        super("Fetch Pending Records");

        url = a; con = b; st = c; rs = d;

        sql = "SELECT * FROM Ledger_Accounting WHERE amt_left>0 ORDER BY ";
        l = new Label("Fetch Pending Records According To");
        b1 = new Button("Due Date");
        b2 = new Button("Due Amount");
        b1.addActionListener(this);
        b2.addActionListener(this);

        setLayout(null);

        l.setBounds(200,100,350,40);
        b1.setBounds(100,200,200,40);
        b2.setBounds(400,200,200,40);

        l.setFont(new Font("Verdana", Font.PLAIN, 20));  
        b1.setFont(new Font("Verdana", Font.PLAIN, 16));  
        b2.setFont(new Font("Verdana", Font.PLAIN, 16));  

        add(l);
        add(b1);
        add(b2);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                dispose();
            }
        });

        setSize(700,440);
        setVisible(true);
        setResizable(false);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        sql = "SELECT * FROM Ledger_Accounting WHERE amt_left>0 ORDER BY ";
        Button b = (Button)e.getSource();
        if(b == b1)
        {
            sql = sql + "due_date ";
        }
        if(b == b2)
        {
            sql = sql + "amt_left DESC ";
        }
        try
        {
            fetchPendingRecord(sql);
        }
        catch(Exception e1){}
    }

    static void fetchPendingRecord(String s) throws Exception
    {
        rs = st.executeQuery(s);

        String output = "";
        while(rs.next())
        {
            for(int i=1;i<=6;i++)
            {
                output = output + rs.getString(i)+ "    ";
            }
            output += "\n";
        }
        JOptionPane.showMessageDialog(null,output);   
    }
}

class CustomerTransaction 
{
    int cust_id;
    String name;
    double amt_paid;
    double amt_left;
    String prod_type;
    String due_date;

    public CustomerTransaction()
    {
        cust_id = 0;
        name = "";
        amt_paid = 0.0;
        amt_left = 0.0;
        prod_type = "";
        due_date = "";
    }

    public CustomerTransaction(int a, String b, double c, double d, String e, String f)
    {
        cust_id = a;
        name = b;
        amt_paid = c;
        amt_left = d;
        prod_type = e;
        due_date = f;
    }

    public int getCust_ID()
    {
        return cust_id;
    }

    public String getName()
    {
        return name;
    }

    public String getProd_type()
    {
        return prod_type;
    }

    public String getDue_date()
    {
        return due_date;
    }

    public double getAmt_paid()
    {
        return amt_paid;
    }

    public double getAmt_left()
    {
        return amt_left;
    }

    public void setCust_ID(int a)
    {
        cust_id = a;
    }

    public void setName(String s)
    {
        name = s;
    }
    
    public void setProd_type(String s)
    {
        prod_type = s;
    }

    public void setDue_date(String s)
    {
        due_date = s;
    }

    public void setAmt_paid(double d)
    {
        amt_paid = d;
    }

    public void setAmt_left(double d)
    {
        amt_left = d;
    }
    
    public String toString()
    {
        String str = "\nCust_ID : " + cust_id + "\nName : " + name + "\nAmount paid : " + amt_paid + "\nAmount left : " + amt_left + "\nProduct type : " + prod_type + "\nDate : " + due_date;
        return str;
    }
}

// Class to insert a record
class AddTransaction extends Frame implements ActionListener,FocusListener
{
    CustomerTransaction c;
    Button ba,bb;
    TextField t1,t2,t3,t4,t5,t6;
    Label l1,l2,l3,l4,l5,l6;
    int no;
    double d1,d2;

    public static String url;
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    public AddTransaction(String a,Connection b,Statement c,ResultSet d) throws Exception
    {
        super("Insert Transaction");
        url = a; con = b; st = c; rs = d;

        no = 0;
        d1 =  0.0; d2 = 0.0;
        l1 = new Label("Enter customer ID");
        l2 = new Label("Enter customer name");
        l3 = new Label("Enter amount paid");
        l4 = new Label("Enter amount left");
        l5 = new Label("Enter product type");
        l6 = new Label("Enter due date");

        t1 = new TextField(""+0);
        t2 = new TextField("");
        t3 = new TextField(""+0.0);
        t4 = new TextField(""+0.0);
        t5 = new TextField("");
        t6 = new TextField("dd-mm-yyyy");

        ba = new Button("Add");
        bb = new Button("Back");

        ba.addActionListener(this);
        bb.addActionListener(this);

        setLayout(new GridLayout(7,2,5,5));

        add(l1);add(t1);
        add(l2);add(t2);
        add(l3);add(t3);
        add(l4);add(t4);
        add(l5);add(t5);
        add(l6);add(t6);
        add(ba);add(bb);

        t1.addFocusListener(this);
        t3.addFocusListener(this);
        t4.addFocusListener(this);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                dispose();
            }
        });

        setSize(500,500);
        setVisible(true);
        setResizable(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        Button b = (Button)e.getSource();

        if(b==bb)
        {
            dispose();
        }
        else
        {
            c = new CustomerTransaction(no,t2.getText(),d1,d2,t5.getText(),t6.getText());
            String query = "SELECT * FROM ledger_Accounting WHERE cust_Id="+no;
            try
            {
                rs = st.executeQuery(query);
                if(rs.next()==false)
                {
                    query = "Insert into ledger_Accounting Values("+c.getCust_ID()+",'"+
                            c.getName()+"',"+c.getAmt_paid()+","+c.getAmt_left()+",'"+c.getProd_type()+
                            "',TO_DATE('"+c.getDue_date()+"','DD-MM-YYYY'))";
                    st.execute(query);
                    JOptionPane.showMessageDialog(null, "Data Added");
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "Record with inputed customer number already exists !");
                }
            }
            catch(Exception e1){JOptionPane.showMessageDialog(null, e1);}
           
        }
    }

    public void focusLost(FocusEvent e)
    {
        TextField t = (TextField)e.getSource();
        
        if(t==t1)
        {
            try
            {
                no = Integer.parseInt(t1.getText());
            }
            catch(Exception e1)
            {
                JOptionPane.showMessageDialog(null,"Enter valid customer ID");
                t1.requestFocus();
            }
        }
        if(t==t3)
        {
            try
            {
                d1 = Double.parseDouble(t3.getText());
            }
            catch(Exception e1)
            {
                JOptionPane.showMessageDialog(null,"Enter valid Amount");
                t3.requestFocus();
            }
        }  
        if(t==t4)
        {
            try
            {
                d2 = Double.parseDouble(t4.getText());
            }
            catch(Exception e1)
            {
                JOptionPane.showMessageDialog(null,"Enter valid Amount");
                t4.requestFocus();
            }
        }  
    }

    public void focusGained(FocusEvent e){}
}

// Class for Updation

class UpdateCustomerRecord extends Frame implements ActionListener
{
    Button b1,b2,b3,b4;
    Label l;
    String sql;
    int no;
    double d;
    String str;

    public static String url;
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    public UpdateCustomerRecord(String a,Connection b,Statement c,ResultSet rd) throws Exception
    {
        super("Update Customer Records");

        url = a; con = b; st = c; rs = rd;
        
        no = 0;
        d = 0.0;
        str = "";

        sql = "UPDATE Ledger_Accounting SET ";
        l = new Label("Update Customer Records");
        b1 = new Button("Name");
        b2 = new Button("Balance Paid");
        b3 = new Button("Add Due Balance");
        b4 = new Button("Product");
        
        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
        b4.addActionListener(this);
        
        setLayout(null);

        l.setBounds(200,100,350,40);
        b1.setBounds(100,200,200,40);
        b2.setBounds(400,200,200,40);
        b3.setBounds(100,260,200,40);
        b4.setBounds(400,260,200,40);
        
        l.setFont(new Font("Verdana", Font.PLAIN, 24));  
        b1.setFont(new Font("Verdana", Font.PLAIN, 16));  
        b2.setFont(new Font("Verdana", Font.PLAIN, 16));  
        b3.setFont(new Font("Verdana", Font.PLAIN, 16));  
        b4.setFont(new Font("Verdana", Font.PLAIN, 16));  
        
        add(l);
        add(b1);
        add(b2);
        add(b3);
        add(b4);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                dispose();
            }
        });

        setSize(700,440);
        setVisible(true);
        setResizable(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        sql = "UPDATE Ledger_Accounting SET  ";
        Button b = (Button)e.getSource();
        if(b == b1)
        {
            try
            {
                no = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Customer ID"));
                String query = "SELECT * FROM ledger_Accounting WHERE cust_Id="+no;
                rs = st.executeQuery(query);
                if(rs.next()==false)
                {
                    JOptionPane.showMessageDialog(null, "Invalid Input");
                }
                else
                {
                    str = JOptionPane.showInputDialog(null, "Enter Updated Customer Name");
                    if(str == "")
                    {
                        JOptionPane.showMessageDialog(null, "Updation cancelled");
                    }
                    else
                    {
                        sql = sql + "name = '" + str + "' WHERE cust_id = " + no;
                        updateRecord(sql);
                    }
                }
            }
            catch(Exception e1){JOptionPane.showMessageDialog(null, e1);}     
        }

        if(b == b2)
        {
            try
            {
                no = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Customer ID"));
                String query = "SELECT * FROM ledger_Accounting WHERE cust_Id="+no;
                rs = st.executeQuery(query);
                if(rs.next()==false)
                {
                    JOptionPane.showMessageDialog(null, "Invalid Input");
                }
                else
                {
                    d = Double.parseDouble(JOptionPane.showInputDialog(null, "Enter Amount Paid"));
                    if(d == 0.0)
                    {
                        JOptionPane.showMessageDialog(null, "Updation cancelled");
                    }
                    else
                    {
                        sql = sql + "amt_paid = amt_paid + " + d + ", amt_left = amt_left - " + d + " WHERE cust_id = " + no;
                        updateRecord(sql);
                    }
                }
            }
            catch(Exception e1){JOptionPane.showMessageDialog(null, e1);}
        }

        if(b == b3)
        {
            try
            {
                no = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Customer ID"));
                String query = "SELECT * FROM ledger_Accounting WHERE cust_Id="+no;
                rs = st.executeQuery(query);
                if(rs.next()==false)
                {
                    JOptionPane.showMessageDialog(null, "Invalid Input");
                }
                else
                {
                    d = Double.parseDouble(JOptionPane.showInputDialog(null, "Enter Amount to be added"));
                    if(d == 0.0)
                    {
                        JOptionPane.showMessageDialog(null, "Updation cancelled");
                    }
                    else
                    {
                        sql = sql + "amt_left = amt_left + " + d + " WHERE cust_id = " + no;
                        updateRecord(sql);
                    }
                }
            }
            catch(Exception e1){JOptionPane.showMessageDialog(null, e1);}
        }

        if(b == b4)
        {
            try
            {
                no = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Customer ID"));
                String query = "SELECT * FROM ledger_Accounting WHERE cust_Id="+no;
                rs = st.executeQuery(query);
                if(rs.next()==false)
                {
                    JOptionPane.showMessageDialog(null, "Invalid Input");
                }
                else
                {
                    str = JOptionPane.showInputDialog(null, "Enter Updated Product Name");
                    if(str == "")
                    {
                        JOptionPane.showMessageDialog(null, "Updation cancelled");
                    }
                    else
                    {
                        sql = sql + "prod_type = '" + str + "' WHERE cust_id = " + no;
                        updateRecord(sql);
                    }
                }
            }
            catch(Exception e1){JOptionPane.showMessageDialog(null, e1);}     
        }
    }

    static void updateRecord(String s) throws Exception
    {
        st.executeUpdate(s);
        JOptionPane.showMessageDialog(null,"Record Updated");   
    }
}

// Class to take UserId Password

class DatabaseAccountDetails extends Frame implements ActionListener
{
    Label l1,l2;
    TextField t1,t2;
    Button b1,b2;
    String userId;
    String Password;

    public DatabaseAccountDetails()
    {
        super("Database Login");

        l1 = new Label("UserId/Connection");
        l2 = new Label("Password");
        t1 = new TextField("");
        t2 = new TextField("");
        b1 = new Button("Login");
        b2 = new Button("Exit");

        setLayout(new GridLayout(3,2,5,5));
        add(l1);add(t1);
        add(l2);add(t2);
        add(b1);add(b2);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });

        b1.addActionListener(this);
        b2.addActionListener(this);

        l1.setFont(new Font("Verdana", Font.PLAIN, 18));  
        l2.setFont(new Font("Verdana", Font.PLAIN, 18));  
        b1.setFont(new Font("Verdana", Font.PLAIN, 18));  
        b2.setFont(new Font("Verdana", Font.PLAIN, 18)); 

        setSize(500,200);
        setVisible(true);
        setResizable(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        Button b = (Button)e.getSource();
        if(b==b2)
        System.exit(0);
        else
        {
            userId = t1.getText();
            Password = t2.getText();
            System.out.println(userId+" "+Password);
            dispose();
        }
    }

    public String getUserId()
    {
        return userId;
    }

    public String getPassword()
    {
        return Password;
    }
}