package motion_movement;

/**
 * Movable objects can be moved by objectMovers
 * 
 * @author Mikko Hilpinen
 * @since 22.12.2014
 */
public interface Movable extends Physical
{	
	/**
	 * @return The objectMover that moves this object
	 */
	public ObjectMover getMover();
}
