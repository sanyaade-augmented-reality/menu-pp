package srdes.menupp;
/**
 *\brief Class that represents an entree.
 */
public class Entree {

	private String name;
	private int id;
	private String image;
	private int description;
	
	public Entree(){
		this.name = null;
		this.id = 0;
		this.image = null;
		this.description = 0;
	}
	
	public Entree(String n, int id, int desc){
		this.name = n;
		this.id = id;
		this.image = "srdes.menupp:drawable/" + n;
		this.description = desc;
	}
	/** 
	 *\brief returns entree id
	 */
	public int getId(){
		return this.id;
	}
	/**
	 *\brief sets entree id*/
	public void setId(int newId){
		this.id = newId;
	}
	/**
	 *\brief returns entree name*/
	public String getName(){
		return this.name;
	}
	/**
	 *\brief sets entree name*/
	public void setName(String newName){
		this.name = newName;
	}
	/**
	 *\brief returns entree image as a string*/
	public String getImage(){
		return this.image;
	}
	/**
	 *\brief returns entree description*/
	public int getDescriptionIndex(){
		return this.description;
	}
}
