package srdes.menupp;
/**
 *\brief Class that represents an entree.
 */
/**
 * 
 * @author dksokolov
 * \brief holds entree information
 */
public class Entree {

	private String name;
	private String fileName;
	private int id;
	private String image;
	private int description;
	
	//Entree constructors
	public Entree(){
		this.name = null;
		this.id = 0;
		this.image = null;
		this.description = 0;
	}
	public Entree(String n, int id, int desc){
		this.fileName = n + ".png";
		this.name = "";
		String[] nameParts = n.split("_");
		for (int i = 0 ; i < nameParts.length ; i++) {
			this.name += nameParts[i].substring(0, 1).toUpperCase() + nameParts[i].substring(1) + ((i == nameParts.length - 1) ? ("") : (" "));
		}		
		this.id = id;
		this.image = "srdes.menupp:assets/" + n;
		this.description = desc;
	}
	/** 
	 *\brief returns entree id
	 */
	//get and set methods
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
	public String getFileName(){
		return this.fileName;
	}
}
