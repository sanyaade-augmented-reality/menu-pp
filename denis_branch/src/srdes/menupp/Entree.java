package srdes.menupp;

public class Entree {

	private String name;
	private int id;
	private String image;
	private String description;
	
	public Entree(){
		this.name = null;
		this.id = 0;
		this.image = null;
		this.description = null;
	}
	
	public Entree(String n, int i){
		/*switch(i){
		case 0: this.name = R.string.entree1; break;
		case 1: this.name = R.string.entree2; break;
		case 2: this.name = R.string.entree3; break;
		case 3: this.name = R.string.entree4; break;
		case 4: this.name = R.string.entree5; break;
		default: this.name = R.string.entree_name;
		}*/
		this.name = n;
		this.id = i;
		this.image = null;
		this.description = null;
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
}
