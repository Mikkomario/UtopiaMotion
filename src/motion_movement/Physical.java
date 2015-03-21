package motion_movement;

import omega_util.GameObject;
import omega_util.Transformable;

/**
 * Physical objects can be part of physical operations like moving and rotating
 * @author Mikko Hilpinen
 * @since 21.3.2015
 */
public interface Physical extends Transformable, GameObject
{
	/**
	 * @return The weight of the object in kilograms
	 */
	public double getMass();
}
