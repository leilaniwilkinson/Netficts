import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.Date;

// Classes to store data in
class CustomerRating {
  int customer_id;
  float rating;
  Date date;
  String title_id;

  // Customer Rating Object Constructor
  public CustomerRating(int customer_id, float rating, Date date, String title_id) {
    this.customer_id = customer_id;
    this.rating = rating;
    this.date = date;
    this.title_id = title_id;
  }

  // Overriding how CustomerRating is output
  public String toString() {
    String output = "\n[ " + 
                    customer_id + ", " + 
                    rating + ", " + 
                    date + ", " + 
                    title_id + 
                    " ]";
    return output;
  }
}

class Title {
  String title_id;
  String title_type;
  String original_title;
  int start_year;
  int runtime_minutes;
  String genres;
  int year;
  float average_rating;
  int num_votes;

  // Title Object Constructor
  public Title(
    String title_id, 
    String title_type, 
    String original_title, 
    int start_year, 
    int runtime_minutes, 
    String genres, 
    int year, 
    float average_rating, 
    int num_votes
  ) {
    this.title_id = title_id;
    this.title_type = title_type;
    this.original_title = original_title;
    this.start_year = start_year;
    this.runtime_minutes = runtime_minutes;
    this.genres = genres;
    this.year = year;
    this.average_rating = average_rating;
    this.num_votes = num_votes;
  }


  // Overriding how Title is output
  public String toString() {
    String output = "\n[ " + 
                    title_id + ", " + 
                    title_type + ", " + 
                    original_title + ", " + 
                    start_year + ", " + 
                    runtime_minutes + ", " + 
                    genres + ", " + 
                    year + ", " + 
                    average_rating + ", " + 
                    num_votes + 
                    " ]";
    return output;
  }
}


public class GUI extends JFrame implements ActionListener {
    static JFrame f;

    public static void main(String[] args) {


      // -=-=-=-=-=-=-=-=-=-=- GUI PORTION -=-=-=-=-=-=-=-=-=-=- //
      JFrame frame = new JFrame("Login Page");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(600, 600);

      // Generates and sets the login page
      loginPage(frame);   

      // Make the frame/window appear
      frame.setVisible(true);  

    }
    



    public static void loginPage(JFrame frame) {
      JPanel loginPage = new JPanel();
      JPanel header = new JPanel();
      JPanel options = new JPanel();
      
      JButton option1 = new JButton("Viewers");
      JButton option2 = new JButton("Analysts");
      JLabel label = new JLabel("Please Select View");

      // Opens the VIEWER LOGIN page
      option1.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
          Viewer.viewerLoginPage(frame);
        }  
      }); 

      // Opens the ANALYST page
      option2.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Analyst.analystPage(frame);
        }
      });

      // Add header and buttons
      header.add(label);
      options.add(option1); 
      options.add(option2);   

      // Sets up the layout
      loginPage.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.fill = GridBagConstraints.HORIZONTAL;

      // Appends to page
      loginPage.add(header, gbc);
      loginPage.add(options, gbc);
      frame.add(loginPage);
    }



    
    // If close button is pressed
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Close")) {
            f.dispose();
        }
    }
}
