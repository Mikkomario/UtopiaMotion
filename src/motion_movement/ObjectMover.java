package motion_movement;

import java.util.ArrayList;
import java.util.List;

import exodus_object.DependentGameObject;
import exodus_util.Transformation;
import genesis_event.Actor;
import genesis_event.HandlerRelay;
import genesis_util.Vector2D;

/**
 * Object mover handles object movement by using velocity and accelration.
 * 
 * @author Mikko Hilpinen
 * @since 21.12.2014
 */
public class ObjectMover extends DependentGameObject<Movable> implements Actor
{
	// ATTRIBUTES	--------------------------
	
	private Vector2D acceleration, velocity, lastAcceleration;
	private List<Impulse> impulses;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new objectMover
	 * 
	 * @param user The user that will be moved by this object
	 * @param handlers The handlers that will handle the mover
	 */
	public ObjectMover(Movable user, HandlerRelay handlers)
	{
		super(user, handlers);
		
		// Initializes attributes
		this.acceleration = Vector2D.zeroVector();
		this.velocity = Vector2D.zeroVector();
		this.lastAcceleration = Vector2D.zeroVector();
		this.impulses = new ArrayList<>();
	}
	
	
	// IMPLEMENTED METHODS	--------------------------

	@Override
	public void act(double duration)
	{
		// Applies the impulses
		if (!this.impulses.isEmpty())
		{
			List<Impulse> remainingImpulses = new ArrayList<>();
			for (Impulse impulse : this.impulses)
			{
				applyForce(impulse.getForceOverTime(duration));
				Impulse remainingImpulse = impulse.withDecreasedDuration(duration);
				
				if (remainingImpulse != null)
					remainingImpulses.add(remainingImpulse);
			}
			this.impulses.clear();
			this.impulses = remainingImpulses;
		}
		
		// Applies the motion
		// Position += velocity * t + (0.5 * lastAcceleration * t^2)
		getMaster().setTrasformation(getMaster().getTransformation().plus(
				Transformation.transitionTransformation(
				getVelocity().times(duration).plus(
				this.lastAcceleration.times(0.5 * Math.pow(duration, 2))))));
		// Adjusts the velocity
		Vector2D averageAcceleration = 
				this.lastAcceleration.plus(getAcceleration()).dividedBy(2);
		this.velocity = this.velocity.plus(averageAcceleration.times(duration));
		
		// Remembers the latest acceleration
		this.lastAcceleration = getAcceleration();
		this.acceleration = Vector2D.zeroVector();
	}
	
	
	// GETTERS & SETTERS	-------------------------
	
	/**
	 * @return The velocity of the object
	 */
	public Vector2D getVelocity()
	{
		return this.velocity;
	}
	
	/**
	 * @return The acceleration of the object
	 */
	public Vector2D getAcceleration()
	{
		return this.acceleration;
	}
	
	/**
	 * Changes the object's velocity
	 * @param v The object's new velocity
	 */
	public void setVelocity(Vector2D v)
	{
		this.velocity = v;
	}
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * Applies the given amount of force into the object
	 * @param f The force vector applied to the object (in newtons or something like that)
	 */
	public void applyForce(Vector2D f)
	{
		// a += f / m
		this.acceleration = this.acceleration.plus(f.dividedBy(getMaster().getMass()));
	}
	
	/**
	 * Applies the given impulse to the object. The impulse will take effect over time.
	 * @param i The impulse applied to this object.
	 */
	public void applyImpulse(Impulse i)
	{
		this.impulses.add(i);
	}
	
	/**
	 * Removes all the impulses affecting the object
	 */
	public void negateImpulses()
	{
		this.impulses.clear();
	}
}
