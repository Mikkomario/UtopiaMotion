package motion_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import genesis_event.Actor;
import genesis_event.Drawable;
import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_event.MouseEvent;
import genesis_event.MouseEvent.MouseButtonEventType;
import genesis_event.MouseListener;
import genesis_event.StrictEventSelector;
import genesis_event.MouseEvent.MouseButton;
import genesis_util.HelpMath;
import genesis_util.StateOperator;
import genesis_util.Vector3D;
import motion_movement.Movable;
import motion_movement.ObjectMover;
import motion_movement.ObjectRotator;
import motion_movement.Rotateable;
import omega_util.SimpleGameObject;
import omega_util.Transformable;
import omega_util.Transformation;
import conflict_collision.CollisionChecker;
import conflict_collision.CollisionEvent;
import conflict_collision.CollisionInformation;
import conflict_collision.CollisionListener;

/**
 * These physics objects are for testing the super magic secret algorithm for collision physics
 * @author Mikko Hilpinen
 * @since 22.3.2015
 */
public class TestPhysicObject extends SimpleGameObject implements Movable,
		Rotateable, CollisionListener, MouseListener, Drawable, Actor
{
	// ATTRIBUTES	------------------------
	
	private Transformation t;
	private CollisionInformation collisionInformation;
	private StrictEventSelector<MouseEvent, MouseEvent.Feature> selector;
	private CollisionChecker collisionChecker;
	private ObjectMover mover;
	private ObjectRotator rotator;
	private Vector3D windowSize;
	
	
	// CONSTRUCTOR	------------------------
	
	/**
	 * Creates a new physic object to the desired location
	 * @param handlers The handlers that will handle the object
	 * @param position The position where the object will be placed to
	 * @param windowSize The size of the window
	 */
	public TestPhysicObject(HandlerRelay handlers, Vector3D position, Vector3D windowSize)
	{
		super(handlers);
		
		Vector3D[] vertices = {new Vector3D(0, -50), new Vector3D(-30, 0), 
				new Vector3D(0, 50), new Vector3D(30, 0)};
		
		this.t = new Transformation(position);
		this.collisionInformation = new CollisionInformation(vertices);
		this.selector = MouseEvent.createMouseButtonSelector(MouseButton.LEFT);
		this.selector.addRequiredFeature(MouseButtonEventType.PRESSED);
		this.collisionChecker = new CollisionChecker(this, true, true);
		this.mover = new ObjectMover(this, handlers);
		this.rotator = new ObjectRotator(this, handlers);
		this.windowSize = windowSize;
	}

	@Override
	public double getMass()
	{
		return 10;
	}

	@Override
	public Transformation getTransformation()
	{
		return this.t;
	}

	@Override
	public void setTrasformation(Transformation t)
	{
		this.t = t;
	}

	@Override
	public StateOperator getCanBeCollidedWithStateOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public CollisionInformation getCollisionInformation()
	{
		return this.collisionInformation;
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		if (getTransformation() == null || getCollisionInformation() == null)
			return;
		
		g2d.setColor(Color.BLACK);
		AffineTransform lastTransform = g2d.getTransform();
		getTransformation().transform(g2d);
		getCollisionInformation().drawCollisionArea(g2d);
		g2d.setTransform(lastTransform);
	}

	@Override
	public int getDepth()
	{
		return 0;
	}

	@Override
	public StateOperator getIsVisibleStateOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public StateOperator getListensToMouseEventsOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public EventSelector<MouseEvent> getMouseEventSelector()
	{
		return this.selector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector3D position)
	{
		return false;
	}

	@Override
	public void onMouseEvent(MouseEvent event)
	{
		// When mouse is pressed near, adds force
		Vector3D r = getTransformation().getPosition().minus(event.getPosition());
		
		if (r.getLength() < 200)
		{
			Vector3D force = r.withLength(30);
			applyForce(force, event.getPosition());
		}
	}

	@Override
	public CollisionChecker getCollisionChecker()
	{
		return this.collisionChecker;
	}

	@Override
	public StateOperator getListensForCollisionStateOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public void onCollisionEvent(CollisionEvent event)
	{
		// Only acts if listener
		if (event.getListener().equals(this) && event.getTarget() instanceof TestPhysicObject)
		{
			TestPhysicObject target = (TestPhysicObject) event.getTarget();
			Vector3D collisionPoint = HelpMath.getAveragePoint(event.getCollisionPoints());
			
			Vector3D impulse = ObjectRotator.getCollisionImpulse(this, target, 
					getMover().getVelocity(), target.getMover().getVelocity(), 0, 
					event.getMTV(), collisionPoint);
			Vector3D force = impulse;//.dividedBy(event.getDuration());
					
			//System.out.println(event.getDuration());
			//System.out.println(getMover().getVelocity().getLength() + target.getMover().getVelocity().getLength());
			
			applyForce(force, collisionPoint);
			target.applyForce(force.reverse(), collisionPoint);
			//getMover().handleCollisionWith(target, event.getDuration(), event.getMTV());
			Transformable.transform(this, Transformation.transitionTransformation(event.getMTV().plus(1)));
		}
		else if (event.getTarget() instanceof TestWall)
		{
			Vector3D collisionPoint = HelpMath.getAveragePoint(event.getCollisionPoints());
			
			Vector3D impulse = ObjectRotator.getCollisionImpulse(this, 
					getMover().getVelocity(), 0, event.getMTV(), collisionPoint);
			Vector3D force = impulse;//.dividedBy(event.getDuration());
			
			applyForce(force, collisionPoint);
			//getMover().handleCollisionWith(target, event.getDuration(), event.getMTV());
			Transformable.transform(this, Transformation.transitionTransformation(event.getMTV().plus(1)));
		}
	}

	@Override
	public ObjectRotator getRotator()
	{
		return this.rotator;
	}

	@Override
	public double getDefaultMomentMass()
	{
		return Rotateable.getCylinderMomentMass(getMass(), 
				getCollisionInformation().getRadius());
	}

	@Override
	public ObjectMover getMover()
	{
		return this.mover;
	}

	@Override
	public void act(double duration)
	{
		if (getTransformation() == null || getMover() == null || this.windowSize == null)
			return;
		/*
		Vector3D position = getTransformation().getPosition();
		Vector3D oldVelocity = getMover().getVelocity();
		Vector3D newVelocity = null;
		
		if (position.getFirst() < 0)
			newVelocity = new Vector3D(Math.abs(oldVelocity.getFirst()), oldVelocity.getSecond());
		else if (position.getFirst() > this.windowSize.getFirst())
			newVelocity = new Vector3D(-Math.abs(oldVelocity.getFirst()), oldVelocity.getSecond());
		else if (position.getSecond() < 0)
			newVelocity = new Vector3D(oldVelocity.getFirst(), Math.abs(oldVelocity.getSecond()));
		else if (position.getSecond() > this.windowSize.getSecond())
			newVelocity = new Vector3D(oldVelocity.getFirst(), -Math.abs(oldVelocity.getSecond()));
		
		if (newVelocity != null)
			getMover().setVelocity(newVelocity);
			*/
		applyForce(new Vector3D(0, 1), getTransformation().getPosition());
	}
	
	
	// OTHER METHODS	---------------------
	
	private void applyForce(Vector3D force, Vector3D absoluteEffectPoint)
	{
		getMover().applyForce(force);
		getRotator().applyMoment(getTransformation().inverseTransform(force), 
				getTransformation().inverseTransform(absoluteEffectPoint));
	}
}
