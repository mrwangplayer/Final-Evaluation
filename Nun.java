package silentconvent;

/**
 * Represents a nun character in the Silent Convent game.
 * Each nun has a name and a distinctive trait that defines their
 * personality/role.
 * 
 * Examples:
 * - Agnes: The organized leader
 * - Elara: The quiet observer
 * - Miriam: The spirited one
 * 
 * This class is used to store and display information about the nun characters.
 */
public class Nun {
    /** The nun's name (e.g., "Agnes", "Elara", "Miriam") */
    public String name;

    /**
     * The nun's distinctive trait or characteristic (e.g., "The calm one", "The
     * listener")
     */
    public String trait;

    /**
     * Create a new Nun character.
     * 
     * @param name  The nun's name
     * @param trait The nun's defining trait or personality description
     */
    public Nun(String name, String trait) {
        this.name = name;
        this.trait = trait;
    }
}
