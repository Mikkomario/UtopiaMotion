package motion_movement;

import exodus_object.DependentGameObject;
import exodus_util.Transformation;
import genesis_event.Actor;
import genesis_event.HandlerRelay;

/**
 * ObjectRotator handles object rotation.
 * 
 * @author Mikko Hilpinen
 * @since 23.12.2014
 */
public class ObjectRotator extends DependentGameObject<Rotateable> implements Actor
{
	// ATTRIBUTES	-------------------------
	
	private double rotation, acceleration, lastAcceleration;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new rotator
	 * 
	 * @param user the object that uses this rotator
	 * @param handlers The handlers that will handle the object
	 */
	public ObjectRotator(Rotateable user, HandlerRelay handlers)
	{
		super(user, handlers);
		
		this.rotation = 0;
		this.acceleration = 0;
		this.lastAcceleration = 0;
	}
	
	
	// IMPLEMENTED METHODS	------------------

	@Override
	public void act(double duration)
	{
		// Applies the motion
		// angle += rotation * t + (0.5 * lastAcceleration * t^2)
		getMaster().setTrasformation(getMaster().getTransformation().plus(
				Transformation.rotationTransformation(getRotation() * duration + 
				(0.5 * this.lastAcceleration * Math.pow(duration, 2)))));
		// Adjusts the rotation
		double averageAcceleration = (this.lastAcceleration + this.acceleration) / 2;
		this.rotation += averageAcceleration * duration;
		
		// Remembers the latest acceleration
		this.lastAcceleration = this.acceleration;
		this.acceleration = 0;
	}

	
	// GETTERS & SETTERS	--------------------
	
	/**
	 * @return How fast the object rotates (degrees per step)
	 */
	public double getRotation()
	{
		return this.rotation;
	}
	
	/**
	 * Changes how fast the object rotates
	 * @param newRotation The new rotation speed of the object (degrees per step)
	 */
	public void setRotation(double newRotation)
	{
		this.rotation = newRotation;
	}
	
	
	// OTHER METHODS	-----------------------
	
	/**
	 * Accelerates the object's rotation
	 * @param acceleration How much the object's rotation is increased
	 */
	public void increaseRotation(double acceleration)
	{
		this.acceleration += acceleration;
	}
	
	/**
	 * Decreases how fast the object rotates
	 * @param decrease How much the object's rotation is diminished (sign doesn't matter)
	 */
	public void diminishRotation(double decrease)
	{
		if (decrease < 0)
			decrease *= -1;
		
		if (Math.abs(getRotation()) < decrease)
			setRotation(0);
		else if (getRotation() > 0)
			setRotation(getRotation() - decrease);
		else
			setRotation(getRotation() + decrease);
	}
}
