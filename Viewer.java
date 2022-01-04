import java.sql.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;

 

public class Viewer {

  static ArrayList<JPanel> cards;

   
  // ---------- Helper Functions ---------- //
  // Generates the sidebar navigation
  public static void sidebarNavigation(JPanel cards, Container pane, List<String> cardNames, List<String> btnNames) {
    JPanel sideNavigation = new JPanel();
    sideNavigation.setLayout(new BoxLayout(sideNavigation, BoxLayout.Y_AXIS));
    sideNavigation.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));  // Top, left, bottom, right
    sideNavigation.setBackground(Color.gray);


    // Generate Buttons
    ArrayList<JButton> btns = new ArrayList<JButton>();
    btnNames.forEach(name -> btns.add(new JButton(name)));

    // Center buttons on side
    btns.forEach(btn -> btn.setAlignmentX(JButton.CENTER_ALIGNMENT));   

    // Add click event to each button
    CardLayout cardLayout = (CardLayout)(cards.getLayout());
    for (int i = 0; i < btns.size(); i++) {
      final int pos = i;
      btns.get(i).addActionListener(new ActionListener() {
        public void actionPerformed (ActionEvent e) {
          cardLayout.show(cards, cardNames.get(pos));
        }       
      });
    }

    // Add buttons to side navigation
    btns.forEach(btn -> sideNavigation.add(btn));
    
    pane.add(sideNavigation, BorderLayout.WEST);
  }

  // Generates the cards
  public static ArrayList<JPanel> generateCards(JPanel panel, ArrayList<String> titles, int gridY) {
    GridBagConstraints c = new GridBagConstraints();  // Constraint settings holder
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = gridY;
    c.insets = new Insets(0, 0, 0, 5);  // Create space between the title cards
    ArrayList<JPanel> cards = new ArrayList<JPanel>();  // Returning reference so we can remove them later if needed


    //for loop that prints out the top 10 
    for(int i = 0; i < titles.size(); i++){
      JPanel card = new JPanel();
      card.setPreferredSize(new Dimension(290, 160));  // width x height
      card.setBorder(BorderFactory.createRaisedBevelBorder());
      JLabel title = new JLabel(titles.get(i));
      card.add(title);
      c.gridx = i;
      panel.add(card, c);
      cards.add(card);
    }

    return cards;
  }

  // Clears the current set of cards
  public static void removeCards(JPanel panel, ArrayList<JPanel> cards) {
    cards.forEach(card -> panel.remove(card));
  }

  // Adds the page headers
  public static void addLabel(JPanel panel, String title, int x, int y) {
    GridBagConstraints c = new GridBagConstraints(); 
    JLabel label = new JLabel(title);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = x;
    c.gridy = y;  
    panel.add(label, c); 
  }

  // Provides a new layout shorthand (Defaults with horizontal)
  public static GridBagConstraints setLayout(int x, int y) {
    GridBagConstraints c = new GridBagConstraints(); 
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = x;
    c.gridy = y;  
    return c;
  }



  // ---------- Viewer Pages ---------- //
  public static void homePanel(JPanel homePanel, String userID) {

    // Default starter dates
    String startDate  = "2002-1-1";
    String endDate    = "2006-1-1";

    // Page Headers
    addLabel(homePanel, "Home", 0, 0); 
    addLabel(homePanel, "All Time Favorites", 0, 1); 
    addLabel(homePanel, "Watch History", 0, 3);


    // Get data
    Connection conn = DB.getConnection();
    ArrayList<Title> top10 = DB.getTop10(conn, 1950, 2000);
    ArrayList<CustomerRating> watchHistory = DB.getWatchHistory(conn, 10, userID, startDate, endDate);
    DB.closeConnection(conn);


    // Get the titles for Top 10 and Watch History
    ArrayList<String> top10Titles         = new ArrayList<String>();
    ArrayList<String> watchHistoryTitles  = new ArrayList<String>();
    top10.forEach(item                    -> top10Titles.add(item.original_title.toString()));
    watchHistory.forEach(item             -> watchHistoryTitles.add(item.toString()));


    // Generate the card rows for Top 10 and Watch History
    generateCards(homePanel, top10Titles, 2);
    cards = generateCards(homePanel, watchHistoryTitles, 4);

    
    // Start date input
    MaskFormatter dateMask = null;
    try {
      dateMask = new MaskFormatter("####-##-##");
      dateMask.setPlaceholderCharacter('#');
      dateMask.setValidCharacters("0123456789");
    } catch (ParseException e) {
      e.printStackTrace();  // output invalid date
    }
    JFormattedTextField startDateField   = new JFormattedTextField(dateMask);
    JFormattedTextField endDateField     = new JFormattedTextField(dateMask);
    homePanel.add(startDateField, setLayout(1, 3));
    homePanel.add(endDateField, setLayout(2, 3));



    // Update date button functionality
    JButton updateDateSelectionBtn = new JButton("Get History!");
    updateDateSelectionBtn.addActionListener(new ActionListener() {  
      public void actionPerformed(ActionEvent e) { 

        // Retrieve new data
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();

        Connection conn = DB.getConnection();
        ArrayList<CustomerRating> watchHistory = DB.getWatchHistory(conn, 10, userID, startDate, endDate);
        DB.closeConnection(conn);
    
        ArrayList<String> watchHistoryTitles  = new ArrayList<String>();
        watchHistory.forEach(item -> watchHistoryTitles.add(item.toString()));  // Convert to titles

        // Remove old cards and replace with new ones
        removeCards(homePanel, cards);
        cards = generateCards(homePanel, watchHistoryTitles, 4);
      } 
    });
    homePanel.add(updateDateSelectionBtn, setLayout(3, 3));

    /* 
      Date text input examples
      Potentially use one button to read both start and end dates and call query inside button action
      What it does
        Creates a formatted text field that only accepts numbers in the form of ####-##-##
        And creates a usable string variable once button is pressed
      Potential errors/issues
        No default dates on initialization may result in empty cards
        In order to run query functions, must do them inside button actions because of local variables
        May need to change placeholder to 0 or 1 to avoid errors when running the query
        For desired non-string variables, you must parse the text to whatever you want
        Can set a default date but for now I wasn't able to do without it being an empty text field
    */
  }

  public static void recomendationPanel(JPanel panel, String userID) {
    
    // Page header
    addLabel(panel, "Viewer Recommendation", 0, 0); 

    // Get data
    Connection conn = DB.getConnection();
    ArrayList<Title> recommendations = DB.getViewerRecommentations(conn, userID);
    DB.closeConnection(conn);

    // Get the titles 
    ArrayList<String> recommendationTitles    = new ArrayList<String>();
    recommendations.forEach(item             -> recommendationTitles.add(item.original_title.toString()));

    // Generate the cards
    generateCards(panel, recommendationTitles, 2);
  }

  public static void viewerBewarePanel(JPanel panel, String userID) {
    // Page header
    addLabel(panel, "Viewer Beware", 0, 0);

    // Get data
    Connection conn = DB.getConnection();
    ArrayList<String> bewareTitles = DB.getViewerBeware(conn, userID);
    DB.closeConnection(conn);

    // Generate the card rows
    generateCards(panel, bewareTitles, 2);
  }

  public static void directorsChoicePanel(JPanel panel, String userID) {
    // Page header
    addLabel(panel, "Director's Choice", 0, 0);

    // Get data
    Connection conn = DB.getConnection();
    boolean noFavDirectorsList = false;
    ArrayList<String> favDirectors = DB.getFavDirectors(conn, userID);
    ArrayList<String> directorTitles = DB.titlesFromFavDirectors(conn, userID, favDirectors.get(0));
    int count = 0;
    while (count < favDirectors.size()) {
      directorTitles = DB.titlesFromFavDirectors(conn, userID, favDirectors.get(count));

      if (directorTitles.size() != 0) {
        noFavDirectorsList = true;
        break;
      }

      count++;
    }

    if (noFavDirectorsList == false) {
      String genre = DB.getAvgFavGenre(conn, userID);
      directorTitles = DB.titlesFromFavGenre(conn, userID, genre);
    }

    DB.closeConnection(conn);

    // Generate the card rows
    generateCards(panel, directorTitles, 2);
  }


  // ---------- Pages Wrapper ---------- // 
  public static void viewerPage(JFrame frame, String userID) {

    ArrayList<JPanel> pages       = new ArrayList<JPanel>();
    ArrayList<JScrollPane> panes  = new ArrayList<JScrollPane>();
    List<String> btnNames         = Arrays.asList("Home", "Recommendations", "Beware", "Director's Choice");
    List<String> cardNames        = Arrays.asList("Panel 1", "Panel 2", "Panel 3", "Panel 4");


    // New window setup
    JFrame viewerFrame = new JFrame("Viewer Page");  
    viewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    viewerFrame.setSize(600, 600); 
    

    // Generate the pages
    for (int i = 0; i < 4; i++) {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(50, 40, 50, 40));
      pages.add(panel);
      panes.add(new JScrollPane(panel));
    }

    // Add pages to the container
    JPanel pageContainer = new JPanel(new CardLayout());
    for (int i = 0; i < panes.size(); i++) pageContainer.add(panes.get(i), cardNames.get(i));


    // Add your container to the viewerFrame
    Container pane = viewerFrame.getContentPane();
    pane.add(pageContainer, BorderLayout.CENTER);


    // ---- Add Navigation ---- //
    sidebarNavigation(pageContainer, pane, cardNames, btnNames);


    // ---- Pages ---- //
    homePanel(pages.get(0), userID);
    recomendationPanel(pages.get(1), userID);
    viewerBewarePanel(pages.get(2), userID);
    directorsChoicePanel(pages.get(3), userID);
                

    // Open new page
    frame.setVisible(false);  // Close old frame
    viewerFrame.setVisible(true);  // Open new frame
  }

  public static void viewerLoginPage(JFrame frame) {

    // New page/frame setup
    JFrame newFrame = new JFrame("Viewer Login Page");  // creates a frame with title
    newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // actually closes out of program
    newFrame.setSize(600, 600);  // dimension to see frame

    // Setting up the page layout
    JPanel viewerLoginPage = new JPanel();
    GridBagConstraints gbcnt = new GridBagConstraints();
    viewerLoginPage.setLayout(new GridBagLayout());

    // Username text
    gbcnt.fill = GridBagConstraints.HORIZONTAL;
    gbcnt.gridx = 1;
    gbcnt.gridy = 0;
    gbcnt.ipadx = 10;
    JLabel userLabel = new JLabel("Username");
    viewerLoginPage.add(userLabel, gbcnt);

    // Username input
    gbcnt.gridx = 2;
    gbcnt.gridy = 0;
    gbcnt.ipadx = 120;  // Expands text area
    JTextField userTextField = new JTextField();
    viewerLoginPage.add(userTextField, gbcnt);
    
    // Login button
    gbcnt.gridx = 1;
    gbcnt.gridy = 1;
    gbcnt.ipadx = 0;
    gbcnt.fill = GridBagConstraints.NONE;
    gbcnt.gridwidth = 2;
    JButton loginButton = new JButton("Login");
    viewerLoginPage.add(loginButton, gbcnt);

    // Opens the VIEWER page
    Connection conn = DB.getConnection();
    loginButton.addActionListener(new ActionListener() {  
      public void actionPerformed(ActionEvent e) {  
        try {
          String userText;
          userText = userTextField.getText();

          Statement stmt = conn.createStatement();
          String sqlStatement = "SELECT * FROM customer_ratings WHERE customer_id = '" + userText  + "';";
          ResultSet results = stmt.executeQuery(sqlStatement);

          if (results.next()) {  // userid exists
            newFrame.dispose();
            Viewer.viewerPage(frame, userText);
          }

          else {
            JOptionPane.showMessageDialog(null, "Invalid Username");
          }
        } catch (Exception ex) {
          System.out.println(ex);
        }
      }  
    }); 
    

    // Open new page
    frame.setVisible(false);  // Close old frame
    newFrame.add(viewerLoginPage);
    newFrame.setVisible(true);  // Open new frame
  }

}
