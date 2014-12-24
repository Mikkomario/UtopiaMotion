package motion_movement;

import omega_util.GameObject;
import omega_util.Transformable;

/**
 * Movable objects can be moved by objectMovers
 * 
 * @author Mikko Hilpinen
 * @since 22.12.2014
 */
public interface Movable extends Transformable, GameObject
{
	/**
	 * @return The weight of the object in kilos
	 */
	public double getMass();
	
	/**
	 * @return The objectMover that moves this object
	 */
	public ObjectMover getMover();
}
