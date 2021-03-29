/* 
 * CollisionController.java
 * 
 * Unless you are making a point-and-click adventure game, every single 
 * game is going to need some sort of collision detection.  In a later 
 * lab, we will see how to do this with a physics engine. For now, we use
 * custom physics. 
 * 
 * This class is an example of subcontroller.  A lot of this functionality
 * could go into GameMode (which is the primary controller).  However, we
 * have factored it out into a separate class because it makes sense as a
 * self-contained subsystem.  Note that this class needs to be aware of
 * of all the models, but it does not store anything as fields.  Everything
 * it needs is passed to it by the parent controller.
 * 
 * This class is also an excellent example of the perils of heap allocation.
 * Because there is a lot of vector mathematics, we want to make heavy use
 * of the Vector2 class.  However, every time you create a new Vector2 
 * object, you must allocate to the heap.  Therefore, we determine the
 * minimum number of objects that we need and pre-allocate them in the
 * constructor.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.main.controller;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.main.model.Icicle;
import edu.cornell.gdiac.main.model.Penguin;

import java.util.ArrayList;


/**
 * Controller implementing simple game physics.
 *  
 * This is the simplest of physics engines.  In later labs, we 
 * will see how to work with more interesting engines.
 */
public class CollisionController {

	/** Impulse for giving collisions a slight bounce. */
	public static final float COLLISION_COEFF = 0.1f;
	
	/** Caching object for computing normal */
	private Vector2 normal;

	/** Caching object for computing net velocity */
	private Vector2 velocity;
	
	/** Caching object for intermediate calculations */
	private Vector2 temp;

	/**
     * Contruct a new controller. 
     * 
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     */
	public CollisionController() { 
		velocity = new Vector2();
		normal = new Vector2();
		temp = new Vector2();
	}

	public boolean checkForCollision(ArrayList<Penguin> pList, Icicle i, boolean b){
		if (b){
			i.setActive(true);
			i.setAwake(true);
			return true;
		} else{
			for (Penguin p: pList){
				float dist = p.getPosition().dst(i.getPosition());
				if (dist < 0.8){
					i.setActive(true);
					i.setAwake(true);
					return true;
				}
			}
			return false;
		}

	}

//	/**
//	 *  Handles collisions between ships, causing them to bounce off one another.
//	 *
//	 *  This method updates the velocities of both ships: the collider and the
//	 *  collidee. Therefore, you should only call this method for one of the
//	 *  ships, not both. Otherwise, you are processing the same collisions twice.
//	 *
//	 *  @param ship1 First ship in candidate collision
//	 *  @param ship2 Second ship in candidate collision
//	 */
//	public void checkForCollision(Ship ship1, Ship ship2) {
//		// Calculate the normal of the (possible) point of collision
//		normal.set(ship1.getPosition()).sub(ship2.getPosition());
//		float distance1 = normal.len();
//
//		normal.set(ship1.getPosition()).sub(ship2.getPosition2());
//		float distance2 = normal.len();
//
//		normal.set(ship1.getPosition2()).sub(ship2.getPosition());
//		float distance3 = normal.len();
//
//		normal.set(ship1.getPosition2()).sub(ship2.getPosition2());
//		float distance4 = normal.len();
//
//		float distance = Math.min(Math.min(distance1, distance2), Math.min(distance3, distance4));
//		float impactDistance = (ship1.getDiameter() + ship2.getDiameter()) / 2f;
//		if (distance == distance1){
//			normal.set(ship1.getPosition()).sub(ship2.getPosition());
//		} else if (distance == distance2){
//			normal.set(ship1.getPosition()).sub(ship2.getPosition2());
//		} else if (distance == distance3){
//			normal.set(ship1.getPosition2()).sub(ship2.getPosition());
//		} else{
//			normal.set(ship1.getPosition2()).sub(ship2.getPosition2());
//		}
//		normal.nor();
//
//		// If this normal is too small, there was a collision
//		if (distance < impactDistance) {
//			// "Roll back" time so that the ships are barely touching (e.g. point of impact).
//			// We need to use temp, as the method scl would change the contents of normal!
//			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d1 - dist)/2
//
//			ship1.getPosition().add(temp);
//			ship1.getPosition2().add(temp);
//			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d2 - dist)/2
//
//			ship2.getPosition().sub(temp);
//			ship2.getPosition2().sub(temp);
//
//			// Now it is time for Newton's Law of Impact.
//			// Convert the two velocities into a single reference frame
//			velocity.set(ship1.getVelocity()).sub(ship2.getVelocity()); // v1-v2
//
//			// Compute the impulse (see Essential Math for Game Programmers)
//			float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
//							(normal.dot(normal) * (1 / ship1.getMass() + 1 / ship2.getMass()));
//
//			// Change velocity of the two ships using this impulse
//			temp.set(normal).scl(impulse / ship1.getMass());
//			ship1.getVelocity().add(temp);
//
//
//			temp.set(normal).scl(impulse / ship2.getMass());
//			ship2.getVelocity().sub(temp);
//		}
//	}
//
//	/**
//	 *  Handles collisions between ships and photons, causing them to bounce off one another.
//	 *
//	 *  This method updates the velocities of both the ship and the photons: the collider and the
//	 *  collidee.
//	 *
//	 *  @param ship1 First ship in candidate collision
//	 *  @param photonsQ PhotonQueue for the second ship
//	 *  @param  shipType An integer representing color of the ship
//	 *
//	 */
//	public void checkForCollisionPhoton(Ship ship1, PhotonQueue photonsQ, int shipType) {
//		// Calculate the normal of the (possible) point of collision
//		float impactDistance = (ship1.getDiameter()) / 2f;
//		for (int i = 0; i < photonsQ.size; i++){
//			int index = (photonsQ.head + i) % 512;
//			if (photonsQ.queue[index].type != shipType){
//				normal.set(ship1.getPosition()).sub(new Vector2(photonsQ.queue[index].x, photonsQ.queue[index].y));
//				float distance = normal.len();
//				normal.nor();
//				if(distance <= impactDistance){
//
//					//ship1.getPosition().add(new Vector2(photonsQ.queue[index].vx, photonsQ.queue[index].vy));
//
//					velocity.set(ship1.getVelocity()).sub(new Vector2(photonsQ.queue[index].vx, photonsQ.queue[index].vy));
//
//					// Compute the impulse (see Essential Math for Game Programmers)
//					float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
//							(normal.dot(normal) * (11 / ship1.getMass()));
//
//					// Change velocity of the two ships using this impulse
//					temp.set(normal).scl(impulse / ship1.getMass());
//					ship1.getVelocity().add(temp);
//					ship1.setHit(ship1.getHit() + 1);
//
//
//					temp.set(normal).scl(impulse / (ship1.getMass()/10));
//					photonsQ.queue[index].vx -= temp.x;
//					photonsQ.queue[index].vy -= temp.y;
//				}
//			}
//		}
//
//	}

//	/**
//	 * Check if any one of the ship has been hit 300 or more than 300 times.
//	 * If so, quit the game.
//	 * @param shipBlue
//	 * @param shipRed
//	 */
//	public void checkForDefeat(Ship shipBlue, Ship shipRed){
//		if (shipBlue.getHit() >= 300 || shipRed.getHit() >= 300){
//			System.exit(0);
//		}
//	}

}