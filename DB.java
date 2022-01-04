import java.sql.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


class ActorPair {
   String actorID_1;
   String actorID_2;
   Double averageRating;
   int numVotes;

   ActorPair(String id_1, String id_2, Double _averageRating, int _numVotes) {
      actorID_1 = id_1;
      actorID_2 = id_2;
      averageRating = _averageRating;
      numVotes = _numVotes;
   }

   public String combine() {
      return actorID_1 + "-" + actorID_2;
   }

   public String toString() {
      String output = "<" + actorID_1 + ", " + actorID_2 + ", " + averageRating + ">";
      return output;
   }

   public void updateAverage(ActorPair pair) {
      averageRating = ((averageRating * numVotes) + (pair.averageRating * pair.numVotes)) / (numVotes + pair.numVotes);
   }
}

class ActorGroup {

   String titleID;
   ArrayList<String> actorIDs;
   Double averageRating;
   int numVotes;

   ActorGroup(String _titleID, Double _averageRating, int _numVotes) {
      titleID = _titleID;
      actorIDs = new ArrayList<String>();
      averageRating = _averageRating;
      numVotes = _numVotes;
   }

   public void addActor(String actorID) {
      actorIDs.add(actorID);
   }


   public ArrayList<ActorPair> getActorPairs() {
      ArrayList<ActorPair> pairs = new ArrayList<ActorPair>();

      for (int front = 0; front < actorIDs.size(); front++) {
         for (int back = front + 1; back < actorIDs.size(); back++) {
            pairs.add(new ActorPair(actorIDs.get(front), actorIDs.get(back), averageRating, numVotes));
         }
      }

      return pairs;
   }

   public String toString() {
      String actors = "[";
      for (int i = 0; i < actors.length(); i++) {
         actors = actors + actorIDs.get(i) + ", ";
      }
      actors += "]";

      return "<" + titleID + ", " + averageRating + ", " + actors + ", " + numVotes + ">";
   }

   

   
}

// Database class
public class DB { 

   public static ArrayList<String> ftnMovieList(Connection conn, String beginMovie, String endMovie){

      ArrayList<String> tomatoChain = new ArrayList<String>();
      ArrayList<String> prevVisited = new ArrayList<String>();
      //results.add(beginMovie);
      //logic!        
  
      String bTitleID = beginMovie;
      String eTitleID = endMovie;
      tomatoChain.add("Media: " + bTitleID);
  
      try {
  
        Statement stmt;
        String sqlStatement;
        ResultSet results;
  
        // titles retrieved ===================================
        boolean endTitleFound = false;
        while(!endTitleFound){
          stmt = conn.createStatement();
          sqlStatement = "SELECT * FROM customer_ratings" + 
                        " WHERE title_id = '" + bTitleID +
                        "' AND (rating = 4 OR rating = 5)";
          results = stmt.executeQuery(sqlStatement);
  
          String lastCustomer = "";
          while (results.next() && !endTitleFound){
            stmt = conn.createStatement();
            String customerID = results.getString("customer_id");
            sqlStatement = "SELECT * FROM customer_ratings " + 
                          "WHERE customer_id = " + customerID + 
                          " AND title_id = '" + eTitleID + "'" + 
                          " AND (rating = " + 4 +
                          "OR rating = " + 5 + 
                          ");";
          
            ResultSet customerResults = stmt.executeQuery(sqlStatement);
            lastCustomer = results.getString("customer_id");
            int rows = 0;
            while(customerResults.next()){
              rows++;
            }
            endTitleFound = (rows > 0); //will only =true if a suitable title is found
          }
            //System.out.println("final cid: " + cid);
           
          if(!endTitleFound){
            tomatoChain.add("User: " + lastCustomer);
            
            stmt = conn.createStatement();
            sqlStatement = "SELECT * FROM customer_ratings\n" + 
                          "WHERE customer_id = " + lastCustomer + "\n" +
                          "AND rating >= 4;";
            int hvViews = 0;
            ResultSet intermediateTitles = stmt.executeQuery(sqlStatement);
            while(intermediateTitles.next()){
              stmt = conn.createStatement();
              sqlStatement = "SELECT * FROM titles\n" +
                            "WHERE title_id = '" + intermediateTitles.getString("title_id") + 
                            "';";
              ResultSet titleInfo = stmt.executeQuery(sqlStatement);
              if(titleInfo.next()){
              if(!prevVisited.contains(titleInfo.getString("title_id"))){
              if(titleInfo.getInt("num_votes") > hvViews){
                bTitleID = titleInfo.getString("title_id");
              }
              }
              }
            }
            tomatoChain.add("Media: " + bTitleID);
            prevVisited.add(bTitleID);
          }
          
           else {
            tomatoChain.add("User: " + lastCustomer);
            tomatoChain.add("Media: " + eTitleID);
          }
        
        }
      
      } catch (Exception e){
              JOptionPane.showMessageDialog(null,e);
      }
  
          return tomatoChain;
  
      }


      
   public static void insertPair(ArrayList<ActorPair> hollywoodPairs, ActorPair pair) {
      int cap = Math.min(10, hollywoodPairs.size());

      for (int i = 0; i < cap; i++) {
         if (pair.averageRating > hollywoodPairs.get(i).averageRating) {
            hollywoodPairs.add(i, pair);
            break;
         }
      }

   }


   public static ArrayList<ActorPair> hollywoodPairs(ArrayList<ActorGroup> groups) {
      ArrayList<ActorPair> hollywoodPairs = new ArrayList<ActorPair>();
      hollywoodPairs.add(0, new ActorPair("00", "00", -1.0, -1));
      HashMap<String, ActorPair> hash = new HashMap<String, ActorPair>();

      groups.forEach(group -> {
         group.getActorPairs().forEach(pair -> {
            String pairID = pair.combine();
            
            if (hash.containsKey(pairID))    hash.get(pairID).updateAverage(pair);     // If it exists, then update its average
            else                             hash.put(pairID, pair);    // If hash doesn't exist, initialize it in the table

            // Maintain hollywood pairs
            insertPair(hollywoodPairs, pair);
         });
      });   
      
      return hollywoodPairs;
   }

   public static ArrayList<ActorGroup> getActors(Connection conn) {
      ArrayList<ActorGroup> groups = new ArrayList<ActorGroup>();
      groups.add(new ActorGroup("000000", -1.0, -1));  // Dummy node to initialize array to 1

      try {

         // SQL Statement Execution
         Statement stmt = conn.createStatement();
         String sqlStatement  = "SELECT titles.title_id, nconst, average_rating, num_votes " 
                              + "FROM titles " 
                              + "INNER JOIN principals ON principals.title_id = titles.title_id "
                              + "WHERE principals.category = 'actor' or principals.category = 'actress' "
                              + "ORDER BY titles.title_id ASC;";
         ResultSet results = stmt.executeQuery(sqlStatement);


         // Get all results
         while (results.next()) {
            int groupCount = groups.size();

            // Retrieve all title attributes
            String titleID          = results.getString("title_id");
            String actorID          = results.getString("nconst");
            Double averageRating    = results.getDouble("average_rating");
            int numVotes            = results.getInt("num_votes");

            // Generate new title object
            if (groups.get(groupCount - 1).titleID.equals(titleID)) {    // If still adding to same title
               groups.get(groupCount - 1).addActor(actorID);
            } else {
               ActorGroup group = new ActorGroup(titleID, averageRating, numVotes);
               groups.add(group);
            }
         }
         groups.remove(0);    // Remove dummy node


      } catch (Exception e){
         JOptionPane.showMessageDialog(null,e);
      }
      return groups;
   }


   public static ArrayList<String> getCultClassics(Connection conn) {
      ArrayList<String> cultClassics = new ArrayList<String>();
    
      try {
    
        // SQL Statement Execution
        Statement stmt = conn.createStatement();
        String sqlStatement = "SELECT COUNT(customer_ratings.customer_id), customer_ratings.title_id, titles.original_title "
                              + "\nFROM customer_ratings "
                              + "\nINNER JOIN titles ON customer_ratings.title_id=titles.title_id "
                              + "\nWHERE customer_ratings.rating >= 4 "
                              + "\nGROUP BY customer_ratings.title_id, titles.original_title "
                              + "\nORDER BY count DESC LIMIT 10;";
    
        ResultSet results = stmt.executeQuery(sqlStatement);
    
        // Get all results
        while (results.next()) {
          // Retrieve all title attributes
          String title = results.getString("original_title");
          cultClassics.add(title);   // Add title to list
        }
        
      } catch (Exception e){
        JOptionPane.showMessageDialog(null,e);
      }
    
      return cultClassics;
    }      
    

   

   // Viewer Beware - find user with similar dislikes
   public static String getSimilarDislikesUser(Connection conn, String userID) {
      String similarDislikesUser = "";

      try {

         Statement stmt = conn.createStatement();
         String sqlStatement = "SELECT B.customer_id, COUNT(B.rating) "
                                 + "FROM customer_ratings A, customer_ratings B "
                                 + "WHERE A.customer_id = " // Current user
                                 + userID
                                 + " AND A.customer_id != B.customer_id "
                                 + "AND A.rating < 3 "
                                 + "AND B.rating < 3 "
                                 + "AND A.title_id = B.title_id "
                                 + "GROUP BY B.customer_id "
                                 + "ORDER BY COUNT DESC "
                                 + "LIMIT 1;";  // Most matching dislikes user

         ResultSet results = stmt.executeQuery(sqlStatement);
         results.next();
         similarDislikesUser = results.getString("customer_id");
         System.out.println(similarDislikesUser);

      } catch (Exception e){
        JOptionPane.showMessageDialog(null,e);
      }

      return similarDislikesUser;
   }

   // Viewer Beware - gets titles disliked from user b that user a has not seen
   public static ArrayList<String> getViewerBeware(Connection conn, String userID) {
      ArrayList<String> viewerBeware = new ArrayList<String>();

      String userIDB = getSimilarDislikesUser(conn, userID);

      try {

         // SQL Statement Execution
         Statement stmt = conn.createStatement();
         String sqlStatement = "SELECT DISTINCT titles.original_title "
                                 + "FROM customer_ratings A, customer_ratings B "
                                 + "INNER JOIN titles ON B.title_id=titles.title_id "
                                 + "WHERE A.customer_id = "  // '3321'  -- Current user 
                                 + userID
                                 + " AND B.customer_id = "  //'2439493'  -- The user with the most same dislikes
                                 + userIDB
                                 + " AND B.rating < 3 "
                                 + "AND A.title_id != B.title_id LIMIT 10;" ;

         ResultSet results = stmt.executeQuery(sqlStatement);

         // Get all results
         while (results.next()) {

            // Retrieve all title attributes
            String title = results.getString("original_title");
            viewerBeware.add(title);   // Add title to list
         }
        
      } catch (Exception e){
         JOptionPane.showMessageDialog(null,e);
      }

      return viewerBeware;
   }  


   // Director's Choice - grabs list of favorite directors
   public static ArrayList<String> getFavDirectors(Connection conn, String userID) {
      ArrayList<String> favDirectors = new ArrayList<String>();

      try {

         // SQL Statement Execution
         Statement stmt = conn.createStatement();
         String sqlStatement = "SELECT regexp_split_to_table(crew.directors, E',') AS split_directors, COUNT(*), AVG(customer_ratings.rating) "  // Separates genres at commas
                                 + "FROM customer_ratings " 
                                 + "INNER JOIN crew ON customer_ratings.title_id = crew.title_id "
                                 + "WHERE customer_ratings.customer_id = "
                                 + userID
                                 + " GROUP BY split_directors "
                                 + "ORDER BY AVG DESC;";

         ResultSet results = stmt.executeQuery(sqlStatement);

         // Get all results
         while (results.next()) {

            // Retrieve all title attributes
            String directors = results.getString("split_directors");
            favDirectors.add(directors);   // Add title to list
         }
        
      } catch (Exception e){
         JOptionPane.showMessageDialog(null,e);
      }

      return favDirectors;
   }  

   // Director's Choice - gets titles from favorite director that user has not yet seen
   public static ArrayList<String> titlesFromFavDirectors(Connection conn, String userID, String directorID) {
      ArrayList<String> directorTitles = new ArrayList<String>();

      try {

         // SQL Statement Execution
         Statement stmt = conn.createStatement();
         String sqlStatement = "SELECT original_title , titles.average_rating "
                                 + " FROM crew C "
                                 + " FULL OUTER JOIN titles ON C.title_id = titles.title_id "
                                 + " FULL OUTER JOIN customer_ratings A ON A.title_id = titles.title_id "
                                 + " WHERE C.directors = '"
                                 + directorID
                                 + "' AND (A.customer_id != "
                                 + userID 
                                 + " OR A.customer_id IS NULL) "
                                 + "AND original_title IS NOT NULL "
                                 + "GROUP BY original_title, titles.average_rating "
                                 + "ORDER BY titles.average_rating DESC;";

         ResultSet results = stmt.executeQuery(sqlStatement);

         // Get all results
         while (results.next()) {

            // Retrieve all title attributes
            String titles = results.getString("original_title");
            directorTitles.add(titles);   // Add title to list
         }
        
      } catch (Exception e){
         JOptionPane.showMessageDialog(null,e);
      }

      return directorTitles;
   }  

   // Director's Choice - return average single favorite genre
   public static String getAvgFavGenre(Connection conn, String userID) {
      String similarDislikesUser = "";

      try {

         Statement stmt = conn.createStatement();
         String sqlStatement = "SELECT B.customer_id, COUNT(B.rating) "
                                 + "FROM customer_ratings A, customer_ratings B "
                                 + "WHERE A.customer_id = " // Current user
                                 + userID
                                 + " AND A.customer_id != B.customer_id "
                                 + "AND A.rating < 3 "
                                 + "AND B.rating < 3 "
                                 + "AND A.title_id = B.title_id "
                                 + "GROUP BY B.customer_id "
                                 + "ORDER BY COUNT DESC "
                                 + "LIMIT 1;";  // Most matching dislikes user

         ResultSet results = stmt.executeQuery(sqlStatement);
         results.next();
         similarDislikesUser = results.getString("split_genres");
         System.out.println(similarDislikesUser);

      } catch (Exception e){
        JOptionPane.showMessageDialog(null,e);
      }

      return similarDislikesUser;
   }

   // Director's Choice - get titles from favorite average genre user has not already seen
   public static ArrayList<String> titlesFromFavGenre(Connection conn, String userID, String genre) {
      ArrayList<String> genreTitles = new ArrayList<String>();

      try {

         // SQL Statement Execution
         Statement stmt = conn.createStatement();
         String sqlStatement = "SELECT titles.original_title ,titles.average_rating "
                                 + "FROM customer_ratings "
                                 + "INNER JOIN titles ON customer_ratings.title_id=titles.title_id "
                                 + "WHERE titles.genres = '"
                                 + genre
                                 + "' AND (customer_ratings.customer_id != "
                                 + userID
                                 + " OR customer_ratings.customer_id IS NULL) "
                                 + "AND titles.original_title IS NOT NULL "
                                 + "GROUP BY original_title, titles.average_rating "
                                 + "ORDER BY titles.average_rating DESC;";

         ResultSet results = stmt.executeQuery(sqlStatement);

         // Get all results
         while (results.next()) {

            // Retrieve all title attributes
            String titles = results.getString("original_title");
            genreTitles.add(titles);   // Add title to list
         }
        
      } catch (Exception e){
         JOptionPane.showMessageDialog(null,e);
      }

      return genreTitles;
   }  


   // Gets the highest rated genre for a given user
   public static String getGenreRecommendation(Connection conn, String userID) {
      String genre = "";
      try {

         // SQL Statement Execution
         Statement stmt = conn.createStatement();
         String sqlStatement = "SELECT AVG(customer_ratings.rating), titles.genres, customer_ratings.customer_id FROM customer_ratings INNER JOIN titles ON customer_ratings.title_id = titles.title_id WHERE customer_ratings.customer_id = " + userID + " GROUP BY customer_ratings.customer_id, titles.genres ORDER BY avg DESC LIMIT 1;";

         ResultSet results = stmt.executeQuery(sqlStatement);
         results.next();
         genre = results.getString("genres");
         System.out.println(genre);

      } catch (Exception e) {
         JOptionPane.showMessageDialog(null,e);
      }

      return genre;
   }

   // Gets the top 10 viewer recommendations based on genre
   public static ArrayList<Title> getViewerRecommentations(Connection conn, String userID) {
      ArrayList<Title> recommendations = new ArrayList<Title>();

      String genre = getGenreRecommendation(conn, userID);

      try {

        // SQL Statement Execution
        Statement stmt = conn.createStatement();
        String sqlStatement = "SELECT * FROM titles WHERE genres LIKE '%" + genre + "%' ORDER BY average_rating DESC LIMIT 10;";

        ResultSet results = stmt.executeQuery(sqlStatement);


        // Get all results
        while (results.next()) {

          // Retrieve all title attributes
          String title_id = results.getString("title_id");
          String title_type = results.getString("title_type");
          String original_title = results.getString("original_title");
          int start_year = results.getInt("start_year");
          int runtime_minutes = results.getInt("runtime_minutes");
          String genres = results.getString("genres");
          int year = results.getInt("year");
          float average_rating = results.getFloat("average_rating");
          int num_votes = results.getInt("num_votes");

          // Generate new title object
          Title title = new Title(
            title_id, 
            title_type, 
            original_title, 
            start_year, 
            runtime_minutes, 
            genres, 
            year, 
            average_rating, 
            num_votes
          );
          recommendations.add(title);   // Add title to list
        }


      } catch (Exception e){
        JOptionPane.showMessageDialog(null,e);
      }

      return recommendations;
    }
   
   
    // Required Query 1
   public static ArrayList<Title> getTop10(Connection conn, int startYear, int endYear) {
      ArrayList<Title> top10 = new ArrayList<Title>();

      try {

         // SQL Statement Execution
         Statement stmt = conn.createStatement();
         String sqlStatement = "SELECT * FROM titles WHERE " 
                              + startYear 
                              + " < year AND year < " 
                              + endYear 
                              + " ORDER BY num_votes DESC LIMIT 10;";
         ResultSet results = stmt.executeQuery(sqlStatement);


         // Get all results
         while (results.next()) {

            // Retrieve all title attributes
            String title_id = results.getString("title_id");
            String title_type = results.getString("title_type");
            String original_title = results.getString("original_title");
            int start_year = results.getInt("start_year");
            int runtime_minutes = results.getInt("runtime_minutes");
            String genres = results.getString("genres");
            int year = results.getInt("year");
            float average_rating = results.getFloat("average_rating");
            int num_votes = results.getInt("num_votes");

            // Generate new title object
            Title title = new Title(
               title_id, 
               title_type, 
               original_title, 
               start_year, 
               runtime_minutes, 
               genres, 
               year, 
               average_rating, 
               num_votes
            );
            top10.add(title);   // Add title to list
         }


      } catch (Exception e){
         JOptionPane.showMessageDialog(null,e);
      }

      return top10;
   }

   // Required Query 2
   public static ArrayList<CustomerRating> getWatchHistory(Connection conn, int limit, String userID, String startDate, String endDate) {
      ArrayList<CustomerRating> watchHistory = new ArrayList<CustomerRating>();

      try {

         // SQL Statement Execution
         Statement stmt = conn.createStatement();
         // Date should be given as xxxx-x-x (Year-Month-Day)
         String sqlStatement = "SELECT * FROM customer_ratings WHERE customer_id = " 
                                 + userID  
                                 + " AND date BETWEEN '" 
                                 + startDate 
                                 + "' AND '" 
                                 + endDate 
                                 + "' ORDER BY date DESC LIMIT " 
                                 + limit 
                                 + ";";
                              
         ResultSet results = stmt.executeQuery(sqlStatement);

         // Get all results
         while (results.next()) {
            // Retrieve all title attributes
            int customer_id = results.getInt("customer_id");
            float rating = results.getFloat("rating");
            Date date = results.getDate("date");
            String title_id = results.getString("title_id");

            // Generate new title object
            CustomerRating customerRating = new CustomerRating(
               customer_id,
               rating,
               date,
               title_id
            );
            watchHistory.add(customerRating);   // Add title to list
         }


      } catch (Exception e){
         JOptionPane.showMessageDialog(null,e);
      }

      return watchHistory;
   }

   // Gets the connection to the database
   public static Connection getConnection() {
      Connection conn = null;
      try {
         Class.forName("org.postgresql.Driver");
         conn = DriverManager.getConnection(
            "jdbc:postgresql://csce-315-db.engr.tamu.edu/csce315904_2db",
            "csce315904_2user", 
            "group2_904"
         );
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      // JOptionPane.showMessageDialog(null,"Opened database successfully");
      return conn;
   }

   // Closes our database connection
   public static void closeConnection(Connection conn) {
      try {
         conn.close();
         // JOptionPane.showMessageDialog(null,"Data Collected, Connection Closed.");
      } catch(Exception e) {
         JOptionPane.showMessageDialog(null,"Connection NOT Closed.");
      }
   }

}
