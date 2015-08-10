package motion_movement;

import java.util.ArrayList;
import java.util.List;

import motion_util.Impulse;
import genesis_event.Actor;
import genesis_event.HandlerRelay;
import genesis_util.ConnectedHandled;
import genesis_util.HelpMath;
import genesis_util.Transformation;
import genesis_util.Vector3D;

/**
 * Object mover handles object movement by using velocity and accelration.
 * 
 * @author Mikko Hilpinen
 * @since 21.12.2014
 */
public class ObjectMover extends ConnectedHandled<Movable> implements Actor
{
	// ATTRIBUTES	--------------------------
	
	private Vector3D acceleration, velocity, lastAcceleration;
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
		this.acceleration = Vector3D.zeroVector();
		this.velocity = Vector3D.zeroVector();
		this.lastAcceleration = Vector3D.zeroVector();
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
		// Position += velocity * t + (0.5 * lastAcceleration * t^2) TODO: DOESN'T WORK PROPERLY
		/*
		getMaster().setTrasformation(getMaster().getTransformation().plus(
				Transformation.transitionTransformation(
				getVelocity().times(duration).plus(
				this.lastAcceleration.times(0.5 * Math.pow(duration, 2))))));
		// Adjusts the velocity
		Vector3D averageAcceleration = 
				this.lastAcceleration.plus(getAcceleration()).dividedBy(2);
		this.velocity = this.velocity.plus(averageAcceleration.times(duration));
		
		// Remembers the latest acceleration
		this.lastAcceleration = getAcceleration();
		*/
		this.velocity = this.velocity.plus(getAcceleration().times(duration));
		getMaster().setTrasformation(getMaster().getTransformation().plus(
				Transformation.transitionTransformation(getVelocity().times(duration))));
		this.acceleration = Vector3D.zeroVector();
	}
	
	
	// GETTERS & SETTERS	-------------------------
	
	/**
	 * @return The velocity of the object. (Pxl / step)
	 */
	public Vector3D getVelocity()
	{
		return this.velocity;
	}
	
	/**
	 * @return The acceleration of the object. (Pxl / step^2)
	 */
	public Vector3D getAcceleration()
	{
		return this.acceleration;
	}
	
	/**
	 * Changes the object's velocity
	 * @param v The object's new velocity
	 */
	public void setVelocity(Vector3D v)
	{
		this.velocity = v;
	}
	
	/**
	 * @return The object's current momentum. (Kg * pxl / step)
	 */
	public Vector3D getMomentum()
	{
		// P = m * v
		return getVelocity().times(getMaster().getMass());
	}
	
	/**
	 * Calculates the object's current directional momentum
	 * @param direction The direction of the momentum
	 * @return How much momentum the object has towards the given direction
	 */
	public Vector3D getMomentum(Vector3D direction)
	{
		return getMomentum().vectorProjection(direction);
	}
	
	/**
	 * Changes the object's momentum
	 * @param newMomentum The object's new momentum
	 * @param duration How long the causing effect lasted
	 */
	public void setMomentum(Vector3D newMomentum, double duration)
	{
		// F = dP / dt
		Vector3D force = (newMomentum.minus(getMomentum())).dividedBy(duration);
		applyForce(force);
	}
	
	/**
	 * Changes the object's directional momentum. This only affects the object's movement on 
	 * a single axis.
	 * @param newMomentum The object's new directional momentum. 
	 * @param duration How long the causing effect lasted
	 */
	public void setDirectionalMomentum(Vector3D newMomentum, double duration)
	{
		// Doesn't work with zero vectors
		if (newMomentum.getLength() < 0.0001)
			return;
		
		// F = dP / dt
		Vector3D force = (newMomentum.minus(getMomentum(newMomentum))).dividedBy(duration);
		applyForce(force);
	}
	
	/**
	 * Negates all of the object's directional momentum
	 * @param axis The axis along which the momentum is negated
	 * @param duration How long the causing effect lasted
	 */
	public void negateDirectionalMomentum(Vector3D axis, double duration)
	{	
		// F = dP / dt where dP = -P
		Vector3D force = getMomentum(axis).dividedBy(-duration);
		applyForce(force);
	}
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * Calculates the force necessary for causing the given momentum
	 * @param momentum The momentum
	 * @param duration The duration of the force
	 * @return The force vector that causes the given momentum over the given duration
	 */
	public static Vector3D getForceCausingMomentum(Vector3D momentum, double duration)
	{
		// F = dP / dt
		return momentum.dividedBy(duration);
	}
	
	/**
	 * Calculates the momentum the object will have after a collision with the given 
	 * object
	 * @param other The other object
	 * @return The momentum this object will have after the collision
	 */
	public Vector3D getMomentumAfterCollisionWith(Movable other)
	{
		double m1 = getMaster().getMass();
		double m2 = other.getMass();
		Vector3D P2 = other.getMover().getMomentum();
		
		// P = (P1 * (m1 - m2) + 2 * P2 * m1) / (m1 + m2)
		return (getMomentum().times(m1 - m2).plus(P2.times(2 * m1))).dividedBy(m1  + m2);
	}
	
	/**
	 * Calculates the directional momentum of the object after a collision with the given 
	 * object
	 * @param other The other object
	 * @param axis The axis along which the collision happened.
	 * @return The object's new directional momentum after a collision with another object
	 */
	public Vector3D getDirectionalMomentumAfterCollisionWith(Movable other, Vector3D axis)
	{
		return getMomentumAfterCollisionWith(other).vectorProjection(axis);
	}
	
	/**
	 * Changes the momentums of the two objects as if they had collided with each other
	 * @param other The object that was collided with
	 * @param collisionDuration How long the collision took place
	 */
	public void handleCollisionWith(Movable other, double collisionDuration)
	{
		Vector3D endMomentumThis = getMomentumAfterCollisionWith(other);
		Vector3D endMomentumOther = other.getMover().getMomentumAfterCollisionWith(getMaster());
		
		setMomentum(endMomentumThis, collisionDuration);
		other.getMover().setMomentum(endMomentumOther, collisionDuration);
	}
	
	/**
	 * Changes the momentums of the two objects as if they had collided with each other. This 
	 * operation takes directional collisions.
	 * @param other The object that was collided with
	 * @param collisionDuration How long the collision took place
	 * @param axis The axis along which the collision happened. A minimum translation vector 
	 * can be used here, for example
	 */
	public void handleCollisionWith(Movable other, double collisionDuration, 
			Vector3D axis)
	{
		Vector3D endMomentumThis = getDirectionalMomentumAfterCollisionWith(other, axis);
		Vector3D endMomentumOther = 
				other.getMover().getDirectionalMomentumAfterCollisionWith(getMaster(), axis);
		
		if (endMomentumThis.getLength() > 0.001)
			setDirectionalMomentum(endMomentumThis, collisionDuration);
		else
			negateDirectionalMomentum(axis, collisionDuration);
		
		if (endMomentumOther.getLength() > 0.001)
			other.getMover().setDirectionalMomentum(endMomentumOther, collisionDuration);
		else
			other.getMover().negateDirectionalMomentum(axis, collisionDuration);
	}
	
	/**
	 * Applies the given amount of force into the object
	 * @param f The force vector applied to the object (Kg * pxl)
	 */
	public void applyForce(Vector3D f)
	{
		// a += f / m
		this.acceleration = this.acceleration.plus(f.dividedBy(getMaster().getMass()));
	}
	
	/**
	 * Applies friction to every direction based on the gravity. This should be used mostly in 
	 * overhead perspectives
	 * @param frictionModifier The friction modifier between the surfaces
	 * @param duration The duration of the effect
	 * @param gravityConstant The gravity constant that affects the force intensity (Kg * pxl)
	 */
	public void applyFriction(double frictionModifier, double duration, double gravityConstant)
	{
		// N = G = m * g
		applyFriction(frictionModifier, duration, gravityConstant * getMaster().getMass(), 
				getVelocity());
	}
	
	/**
	 * Applies friction along the given surface.
	 * @param frictionModifier The friction modifier between the surfaces
	 * @param duration The duration of the effect
	 * @param supportForce How large is the force pushing the surfaces apart
	 * @param surfaceAxis An axis parallel to the collision surface
	 */
	public void applyFriction(double frictionModifier, double duration, 
			double supportForce, Vector3D surfaceAxis)
	{
		applyFriction(frictionModifier, duration, supportForce, surfaceAxis, 
				Vector3D.zeroVector());
	}
	
	/**
	 * Applies friction along the given moving surface.
	 * @param frictionModifier The friction modifier between the surfaces
	 * @param duration The duration of the effect
	 * @param supportForce How large is the force pushing the surfaces apart
	 * @param surfaceAxis An axis parallel to the collision surface
	 * @param surfaceVelocity The velocity of the surface
	 */
	public void applyFriction(double frictionModifier, double duration, 
			double supportForce, Vector3D surfaceAxis, Vector3D surfaceVelocity)
	{
		Vector3D directionalVelocity = getVelocity().vectorProjection(surfaceAxis);
		Vector3D directionalSurfaceVelocity = surfaceVelocity.vectorProjection(surfaceAxis);
		Vector3D velocityDifference = directionalSurfaceVelocity.minus(directionalVelocity);
		
		// If the object isn't moving, doesn't apply friction
		if (HelpMath.areApproximatelyEqual(velocityDifference.getLength(), 0))
			return;
		
		// F = u * N
		Vector3D f = velocityDifference.withLength(frictionModifier * supportForce);
		// Fmax = dP / dt where dP = m * dv
		Vector3D fMax = velocityDifference.times(getMaster().getMass()).dividedBy(duration);
		
		// The friction won't be changing the sign of the velocity
		if (f.getLength() > fMax.getLength())
		{
			if (HelpMath.areApproximatelyEqual(surfaceVelocity.getLength(), 0))
				negateDirectionalMomentum(surfaceAxis, duration);
			// a = dv / dt
			else
				this.acceleration = getAcceleration().plus(velocityDifference.dividedBy(
						duration));
		}
		else
			applyForce(f);
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
	
	/**
	 * Changes the velocity of the object along the given axis
	 * @param v The new directional movement of the object. Will only affect velocity parallel 
	 * to the given vector.
	 */
	public void setDirectionalVelocity(Vector3D v)
	{
		Vector3D directionalVelocity = getVelocity().vectorProjection(v);
		setVelocity(getVelocity().plus(v.minus(directionalVelocity)));
	}
	
	/**
	 * Stops the object along the given axis
	 * @param direction The axis on which the object is stopped
	 */
	public void negateDirectionalVelocity(Vector3D direction)
	{
		Vector3D directionalVelocity = getVelocity().vectorProjection(direction);
		// Only negates the velocity if it is towards the given direction
		if (HelpMath.getAngleDifference180(direction.getZDirection(), 
				directionalVelocity.getZDirection()) < 90)
			setVelocity(getVelocity().minus(directionalVelocity));
	}
	
	/**
	 * Stops the object along the given axis
	 * @param direction The direction on which the object is stopped
	 */
	public void negateDirectionalVelocity(double direction)
	{
		negateDirectionalVelocity(Vector3D.unitVector(direction));
	}
}
