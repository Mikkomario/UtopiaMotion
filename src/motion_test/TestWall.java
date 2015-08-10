package motion_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import genesis_event.Drawable;
import genesis_event.HandlerRelay;
import genesis_util.SimpleHandled;
import genesis_util.Transformation;
import genesis_util.Vector3D;
import conflict_collision.Collidable;
import conflict_collision.CollisionInformation;
import conflict_util.Polygon;

/**
 * This is just a simple wall that can be collided with
 * @author Mikko Hilpinen
 * @since 29.3.2015
 */
public class TestWall extends SimpleHandled implements Collidable, Drawable
{
	// ATTRIBUTES	------------------------------
	
	private Transformation transformation;
	private CollisionInformation collisionInfo;
	
	
	// CONSTRUCTOR	------------------------------
	
	/**
	 * Creates a new wall
	 * @param handlers The handlers that will handle the wall
	 * @param position The position of the wall
	 * @param size The size of the wall
	 */
	public TestWall(HandlerRelay handlers, Vector3D position, Vector3D size)
	{
		super(handlers);
		
		this.transformation = new Transformation(position);
		this.collisionInfo = new CollisionInformation(Polygon.getRectangleVertices(
				Vector3D.zeroVector(), size));
	}
	
	
	// IMPLEMENTED METHODS	----------------------

	@Override
	public Transformation getTransformation()
	{
		return this.transformation;
	}

	@Override
	public void setTrasformation(Transformation t)
	{
		this.transformation = t;
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		AffineTransform lastTransform = g2d.getTransform();
		g2d.setColor(Color.BLACK);
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
	public CollisionInformation getCollisionInformation()
	{
		return this.collisionInfo;
	}
}
