package srdes.menupp;

/**
 * \brief Holds review information for easy passing
 */
public class Review {

	private String title;
    private String body;
    private String entreeName;
    private float rating;
    private int unique_id;
 
    /**
     * Constructor for the review
     * @param id
     * @param title
     * @param body
     * @param name
     * @param rating
     * @return review
     */
    public Review(int id, String title, String body, String name, float rating){
    	this.unique_id = id;
    	this.title = title;
    	this.body = body;
    	this.entreeName = name;
    	this.rating = rating;
    }
    
    //accessor methods
    public int getId(){
    	return this.unique_id;
    }
    
    public String getTitle(){
    	return this.title;
    }
    
    public String getBody(){
    	return this.body;
    }
    
    public String getName(){
    	return this.entreeName;
    }
    
    public float getRating(){
    	return this.rating;
    }
}
