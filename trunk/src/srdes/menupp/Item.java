package srdes.menupp;

public class Item {
	private String details;
	private String name;
	private int price;

	public Item(){

	}

	public Item(String i, String d, int p){
		this.details = d;
		this.name = i;
		this.price = p;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}