package lexicalAnalysis;

public class Token {
	public String species;
	public String attribute;
	
	public Token (String species, String attribute) {
		this.species = species;
		this.attribute = attribute;
	}
	
	public String toString() {
		return "<" + this.species + "," + this.attribute + ">";
	}
}
