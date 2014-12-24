package motion_movement;

import omega_util.GameObject;
import omega_util.Transformable;

/**
 * RotateAble objects can be rotated by objectRotators
 * 
 * @author Mikko Hilpinen
 * @since 23.12.2014
 */
public interface Rotateable extends Transformable, GameObject
{
	/**
	 * @return The object that handles this object's rotation
	 */
	public ObjectRotator getRotator();
}
