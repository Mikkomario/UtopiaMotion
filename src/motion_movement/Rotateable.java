package motion_movement;

/**
 * RotateAble objects can be rotated by objectRotators
 * 
 * @author Mikko Hilpinen
 * @since 23.12.2014
 */
public interface Rotateable extends Physical
{
	/**
	 * @return The object that handles this object's rotation
	 */
	public ObjectRotator getRotator();
	
	/**
	 * @return The object's moment mass when rotating around it's origin (center of mass). In  
	 * kg * pxl^2.
	 */
	public double getDefaultMomentMass();
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * Calculates the moment mass of a dot like object
	 * @param mass The mass of the object
	 * @param radius The radius from the rotation origin
	 * @return The object's moment mass
	 */
	public static double getDotMomentMass(double mass, double radius)
	{
		return mass * Math.pow(radius, 2);
	}
	
	/**
	 * Calculates the moment mass of a cylinder like object
	 * @param mass The mass of the object
	 * @param radius The radius of the cylinder
	 * @return The object's moment mass
	 */
	public static double getCylinderMomentMass(double mass, double radius)
	{
		return 0.5 * getDotMomentMass(mass, radius);
	}
	
	/**
	 * Calculates the moment mass of a circle like object
	 * @param mass The mass of the object
	 * @param radius The radius of the circle
	 * @return The object's moment mass
	 */
	public static double getThinCircleMomentMass(double mass, double radius)
	{
		return getDotMomentMass(mass, radius);
	}
	
	/**
	 * Calculates the moment mass of a circle like object
	 * @param mass The mass of the object
	 * @param outerRadius The radius of the circle
	 * @param innerRadius The radius of the empty are within the circle
	 * @return The object's moment mass
	 */
	public static double getThickCircleMomentMass(double mass, double outerRadius, 
			double innerRadius)
	{
		return 0.5 * mass * (Math.pow(outerRadius, 2) + Math.pow(innerRadius, 2));
	}
	
	/**
	 * Calculates the moment mass of a stick like object
	 * @param mass The mass of the object
	 * @param length The length of the object
	 * @param originAtCenter Is the rotation origin at the center of the stick (true) or at 
	 * the end of the stick (false)
	 * @return The object's moment mass
	 */
	public static double getStickMomentMass(double mass, double length, boolean originAtCenter)
	{
		double ml2 = mass * Math.pow(length, 2);
		if (originAtCenter)
			return ml2 / 12;
		else
			return ml2 / 3;
	}
	
	/**
	 * Calculates the moment mass of a rectangular object
	 * @param mass The mass of the object
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle
	 * @return The object's moment mass
	 */
	public static double getRectangleMomentMass(double mass, double width, double height)
	{
		return mass * (Math.pow(width, 2) + Math.pow(height, 2)) / 12;
	}
	
	/**
	 * Calculates the moment mass of a three dimensional ball
	 * @param mass The mass of the object
	 * @param radius The radius of the ball
	 * @return The object's moment mass
	 */
	public static double getSolidBallMomentMass(double mass, double radius)
	{
		return 2 * mass * Math.pow(radius, 2) / 5;
	}
	
	/**
	 * Calculates the moment mass of a three dimensional ball that is very thin
	 * @param mass The mass of the object
	 * @param radius The radius of the ball
	 * @return The object's moment mass
	 */
	public static double getHollowBallMomentMass(double mass, double radius)
	{
		return 2 * mass * Math.pow(radius, 2) / 3;
	}
}
