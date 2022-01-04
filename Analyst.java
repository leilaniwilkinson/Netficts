import java.sql.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;

public class Analyst {

    

    static ArrayList<JPanel> cards;

   
  // ---------- Helper Functions ---------- //
  // Generates the sidebar navigation
  public static void sidebarNavigation(JPanel cards, Container pane, List<String> cardNames, List<String> btnNames) {
    JPanel sideNavigation = new JPanel();
    sideNavigation.setLayout(new BoxLayout(sideNavigation, BoxLayout.Y_AXIS));
    sideNavigation.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));  // Top, left, bottom, right
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
  public static void homePanel(JPanel panel) {

    JTabbedPane tabbedPane = new JTabbedPane();
      
    Connection conn = DB.getConnection();
    
    JTable table = new JTable(10, 1);
    ArrayList<Title> topTen;

    for (int year = 1950; year < 2000; year += 5){
    table = new JTable(10, 1); //10 rows, 1 column
    table.setBorder(BorderFactory.createEmptyBorder(50, 40,  50, 40));
    table.setRowHeight(60);
    
    topTen = DB.getTop10(conn, year, year + 5);
    
    for (int i = 0; i < 10; i++) table.setValueAt(topTen.get(i), i, 0);

    JScrollPane scrBody = new JScrollPane(table);
    String name = Integer.toString(year) + "-" + Integer.toString(year + 5);
    tabbedPane.addTab(name, scrBody);
    }

    DB.closeConnection(conn);
    panel.setLayout(new BorderLayout());
    panel.add(tabbedPane, BorderLayout.CENTER);
  }

  public static void hollywoodPairsPanel(JPanel panel) {

    // Page header
    addLabel(panel, "Hollywood Pairs", 0, 0);

    // Get data
    Connection conn = DB.getConnection();
    ArrayList<ActorGroup> groups = DB.getActors(conn);
    ArrayList<ActorPair> hollywoodPairs = new ArrayList<ActorPair>(DB.hollywoodPairs(groups).subList(0, 10));

    // Get titles
    ArrayList<String> hollywoodPairsTitles = new ArrayList<String>();
    for (int i = 0; i < hollywoodPairs.size(); i++) {
        hollywoodPairsTitles.add(hollywoodPairs.get(i).toString());
    }

    DB.closeConnection(conn);

    // Generate the card rows
    generateCards(panel, hollywoodPairsTitles, 2);
  }

  public static void cultClassicsPanel(JPanel panel) {
    // Page header
    addLabel(panel, "Cult Classics", 0, 0);

    // Get data
    Connection conn = DB.getConnection();
    ArrayList<String> cultClassics = DB.getCultClassics(conn);
    DB.closeConnection(conn);

    // Generate the card rows
    generateCards(panel, cultClassics, 2);
  }

  public static void freshTomatoNumberPanel(JPanel panel) {
      // Default starter dates
    String beginMovie  = "tt0023158";
    String endMovie    = "tt0304678";

    // Page Headers
    addLabel(panel, "Fresh Tomato Number", 0, 0); 


    // Get data
    Connection conn = DB.getConnection();
    ArrayList<String> ftnTitles = DB.ftnMovieList(conn, beginMovie, endMovie);
    DB.closeConnection(conn);


    // Generate the card rows for Top 10 and Watch History
    cards = generateCards(panel, ftnTitles, 4);

    
    JFormattedTextField startMovieField   = new JFormattedTextField();
    JFormattedTextField endMovieField     = new JFormattedTextField();
    panel.add(startMovieField, setLayout(1, 3));
    panel.add(endMovieField, setLayout(2, 3));



    // Update date button functionality
    JButton updateDateSelectionBtn = new JButton("Update Selection!");
    updateDateSelectionBtn.addActionListener(new ActionListener() {  
      public void actionPerformed(ActionEvent e) { 

        // Retrieve new data
        String begin_movie = startMovieField.getText();
        String end_movie = endMovieField.getText();

        Connection conn = DB.getConnection();
        ArrayList<String> ftnTitles = DB.ftnMovieList(conn, begin_movie, end_movie);
        DB.closeConnection(conn);
    

        // Remove old cards and replace with new ones
        removeCards(panel, cards);
        cards = generateCards(panel, ftnTitles, 4);
      } 
    });
    panel.add(updateDateSelectionBtn, setLayout(3, 3));
  }



  // ---------- Pages Wrapper ---------- // 
  public static void analystPage(JFrame frame) {

    ArrayList<JPanel> pages       = new ArrayList<JPanel>();
    ArrayList<JScrollPane> panes  = new ArrayList<JScrollPane>();
    List<String> btnNames         = Arrays.asList("Home", "Cult Classics", "Hollywood Pairs", "Fresh Tomato Number");
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
    homePanel(pages.get(0));
    cultClassicsPanel(pages.get(1));
    hollywoodPairsPanel(pages.get(2));
    freshTomatoNumberPanel(pages.get(3));
                

    // Open new page
    frame.setVisible(false);  // Close old frame
    viewerFrame.setVisible(true);  // Open new frame
  }

}
