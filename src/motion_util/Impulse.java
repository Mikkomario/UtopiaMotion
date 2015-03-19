package motion_util;

import genesis_util.Vector2D;

/**
 * Impulses are forces that have a certain duration and can affect objects over time. The 
 * impulses are immutable once created.
 * 
 * @author Mikko Hilpinen
 * @since 22.12.2014
 */
public class Impulse
{
	// ATTRIBUTES	----------------------------
	
	private Vector2D force;
	private double duration;
	
	
	// CONSTRUCTOR	----------------------------
	
	/**
	 * Creates a new impulse
	 * 
	 * @param force The force vector of the impulse
	 * @param duration The duration of the impluse's effect
	 */
	public Impulse(Vector2D force, double duration)
	{
		// Initializes attribtes
		this.force = force;
		this.duration = duration;
	}

	
	// GETTERS & SETTERS	---------------------
	
	/**
	 * @return The amount of force applied each step
	 */
	public Vector2D getForceVector()
	{
		return this.force;
	}
	
	/**
	 * @return The amount of steps the impulse will take place
	 */
	public double getDuration()
	{
		return this.duration;
	}
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * Calculates the amount of force applied over the given period of time.
	 * @param t How long is the time the force is applied / generated (in steps)
	 * @return The amount of force applied over the given period of time.
	 */
	public Vector2D getForceOverTime(double t)
	{
		if (getDuration() > t)
			return getForceVector().times(t);
		else
			return getForceVector().times(getDuration());
	}
	
	/**
	 * Creates a new impulse with the decreased duration
	 * @param t How much the duration is decreased (in steps)
	 * @return A new impulse with the decreased duration or null if the new impulse wouldn't 
	 * have any duration left
	 */
	public Impulse withDecreasedDuration(double t)
	{
		if (t > getDuration())
			return null;
		
		return new Impulse(getForceVector(), getDuration() - t);
	}
}
