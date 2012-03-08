package srdes.menupp;

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
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int newId){
		this.id = newId;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String newName){
		this.name = newName;
	}
	
	public String getImage(){
		return this.image;
	}
	
	public int getDescriptionIndex(){
		return this.description;
	}
}
