package motion_movement;

import omega_util.DependentGameObject;
import omega_util.Transformable;
import omega_util.Transformation;
import genesis_event.Actor;
import genesis_event.HandlerRelay;
import genesis_util.Vector2D;

/**
 * ObjectRotator handles object rotation.
 * 
 * @author Mikko Hilpinen
 * @since 23.12.2014
 */
public class ObjectRotator extends DependentGameObject<Rotateable> implements Actor
{
	// ATTRIBUTES	-------------------------
	
	private double rotation, acceleration, lastAcceleration, currentMomentMass;
	private Vector2D rotationOrigin;
	private boolean rotationOriginAtDefault;
	
	
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
		this.rotationOrigin = Vector2D.zeroVector();
		this.currentMomentMass = getMaster().getDefaultMomentMass();
		this.rotationOriginAtDefault = true;
	}
	
	
	// IMPLEMENTED METHODS	------------------

	@Override
	public void act(double duration)
	{
		// Applies the rotation
		// angle += rotation * t + (0.5 * lastAcceleration * t^2)
		double angleIncrement = getRotation() * duration + 
				(0.5 * this.lastAcceleration * Math.pow(duration, 2));
		
		if (this.rotationOriginAtDefault)
			Transformable.transform(getMaster(), 
					Transformation.rotationTransformation(angleIncrement));
		else
			getMaster().setTrasformation(
					getMaster().getTransformation().rotatedAroundRelativePoint(
					angleIncrement, getRotationOrigin()));
		
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
	
	/**
	 * @return The relative coordinates around which the object rotates
	 */
	public Vector2D getRotationOrigin()
	{
		return this.rotationOrigin;
	}
	
	/**
	 * @return The current moment mass of the object (Kg * pxl^2)
	 */
	public double getCurrentMomentMass()
	{
		return this.currentMomentMass;
	}
	
	
	// OTHER METHODS	-----------------------
	
	/**
	 * @return The object's current angular momentum (degrees * Kg * pxl^2 / step)
	 */
	public double getAngularMomentum()
	{
		// P = w * J
		return getRotation() * getCurrentMomentMass();
	}
	
	/**
	 * Applies a moment to the object, possibly making it rotate
	 * @param relativeForce The force vector causing the moment. In relative object space.
	 * @param relativeEffectPoint The relative point where the force is applied to
	 */
	public void applyMoment(Vector2D relativeForce, Vector2D relativeEffectPoint)
	{
		Vector2D r = relativeEffectPoint.minus(getRotationOrigin());
		// M = r x f
		double M = r.crossProductLength(relativeForce);
		
		// Checks if the moment should be negative (Removed at genesis 1.271)
		/*
		double forceDir = relativeForce.getDirection();
		double rDir = r.reverse().getDirection();
		
		if (Math.abs(forceDir - rDir) < 180)
		{
			if (forceDir > rDir)
				M = -M;
		}
		else if (forceDir < rDir)
			M = -M;
		*/
		
		// a += M / J
		increaseRotation(M / getCurrentMomentMass());
	}
	
	/**
	 * Changes the rotation origin of the object. The default rotation origin is (0, 0)
	 * @param newOrigin The new rotation origin (relative point)
	 */
	public void setRotationOrigin(Vector2D newOrigin)
	{
		if (newOrigin.equals(getRotationOrigin()))
			return;
		
		double oldMomentMass = getCurrentMomentMass();
		
		// Changes the moment mass of the object
		if (newOrigin.equals(Vector2D.zeroVector()))
		{
			this.currentMomentMass = getMaster().getDefaultMomentMass();
			this.rotationOriginAtDefault = true;
		}
		else
		{
			// May need to change the origin to (0, 0) first
			if (!this.rotationOriginAtDefault)
				setRotationOrigin(Vector2D.zeroVector());
			
			// Ja = J0 + m * d^2
			double d = newOrigin.getLength();
			this.currentMomentMass = getCurrentMomentMass() + getMaster().getMass() * 
					Math.pow(d, 2);
			this.rotationOriginAtDefault = false;
		}
		
		// Updates the rotation origin
		this.rotationOrigin = newOrigin;
		
		// Updates the rotation speed (w2 = J1 * w1 / J2)
		double newRotation = oldMomentMass * getRotation() / getCurrentMomentMass();
		increaseRotation(newRotation - getRotation());
	}
	
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
