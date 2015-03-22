package motion_movement;

import omega_util.DependentGameObject;
import omega_util.Transformable;
import omega_util.Transformation;
import genesis_event.Actor;
import genesis_event.HandlerRelay;
import genesis_util.Vector3D;

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
	private Vector3D rotationOrigin;
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
		this.rotationOrigin = Vector3D.zeroVector();
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
	public Vector3D getRotationOrigin()
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
	public void applyMoment(Vector3D relativeForce, Vector3D relativeEffectPoint)
	{
		Vector3D r = relativeEffectPoint.minus(getRotationOrigin());
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
	public void setRotationOrigin(Vector3D newOrigin)
	{
		if (newOrigin.equals(getRotationOrigin()))
			return;
		
		double oldMomentMass = getCurrentMomentMass();
		
		// Changes the moment mass of the object
		if (newOrigin.equals(Vector3D.zeroVector()))
		{
			this.currentMomentMass = getMaster().getDefaultMomentMass();
			this.rotationOriginAtDefault = true;
		}
		else
		{
			// Ja = J0 + m * d^2
			double d = newOrigin.getLength();
			this.currentMomentMass = getMaster().getDefaultMomentMass() + 
					getMaster().getMass() * Math.pow(d, 2);
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
	
	/**
	 * Calculates the impulse that should be applied to both of the given bodies upon collision
	 * @param body1 The primary colliding object
	 * @param body2 The secondary colliding object
	 * @param v1 The velocity of the primary object
	 * @param v2 The velocity of the secondary object
	 * @param efficiencyCoefficient How efficient the collision is [0, 1]
	 * @param mtv1 The minimal translation vector for the primary object
	 * @param absoluteContactPoint The collision point
	 * @return The impulse that should be applied to the primary object. The secondary object 
	 * should be affected by impulse.reverse()
	 */
	public static Vector3D getCollisionImpulse(Rotateable body1, 
			Rotateable body2, Vector3D v1, Vector3D v2, double efficiencyCoefficient, 
			Vector3D mtv1, Vector3D absoluteContactPoint)
	{
		// ir = -(1 + e) * vr.dot(n) / 
		// (m1^-1 + m2^-1 + (J1^-1 * (r1 x n) x r1 + J2^-1 * (r2 x n) x r2).dot(n))
		// Where e = efficiencyCoefficient
		// And n = mtv.reverse
		// And vr is the speed difference between the pixels in the objects
		// Body 1 is affected by -jr and that is returned
		
		Vector3D n = mtv1.reverse().normalized();
		double m1Inverse = Math.pow(body1.getMass(), -1);
		double m2Inverse = Math.pow(body2.getMass(), -1);
		double J1Inverse = Math.pow(body1.getRotator().getCurrentMomentMass(), -1);
		double J2Inverse = Math.pow(body2.getRotator().getCurrentMomentMass(), -1);
		
		Vector3D r1 = absoluteContactPoint.minus(body1.getTransformation().getPosition());
		Vector3D r2 = absoluteContactPoint.minus(body2.getTransformation().getPosition());
	
		// vp = v + vw
		Vector3D pointVelocity1 = v1.plus(getRailVelocity(r1, body1.getRotator().getRotation()));
		Vector3D pointVelocity2 = v2.plus(getRailVelocity(r2, body2.getRotator().getRotation()));
		//vr = vp1 - vp2 // TODO: Or is it vp2 - vp1?
		Vector3D vr = pointVelocity1.minus(pointVelocity2);
		
		double jrLength = -(1 + efficiencyCoefficient) * vr.dotProduct(n) / 
				(m1Inverse + m2Inverse + 
				r1.crossProduct(n).times(J1Inverse).crossProduct(r1).plus(
				r2.crossProduct(n).times(J2Inverse).crossProduct(r2)).crossProductLength(n));
		return mtv1.withLength(Math.abs(jrLength));
	}
	
	private static Vector3D getRailVelocity(Vector3D r, double rotationSpeed)
	{
		// v = r * w
		return r.times(rotationSpeed).withZDirection(r.getZDirection() + 90);
	}
}
