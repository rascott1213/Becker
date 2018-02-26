/**
 * Author: Ryan Scott
 * 
 * 
 * 
 */

import becker.robots.*;

//...
import javax.swing.JOptionPane;

//I created an object to assist with keeping track of the robot's collapsing
//boundaries, rather than keep it in the main method. This way, I could 
//potentially integrate this with other robots running in the same city.
class RobotParameters {
    //Though not actually necessary for this assignment (the movingCorners fill
    //all calculation functions), I left the original corners in for possible
    //use in a version that wants to send the robot back out.
    int cornerNW[] = new int[2];
    int cornerNE[] = new int[2];
    int cornerSW[] = new int[2];
    int cornerSE[] = new int[2];
    
    //These variables are so titled because they will constantly be moving and
    //changing while the robot is. Discussed more later.
    int movingCornerNW[] = new int[2];
    int movingCornerNE[] = new int[2];
    int movingCornerSW[] = new int[2];
    int movingCornerSE[] = new int[2];
}

public class RobotSearch {
        
    public static void main(String[] args) {
    
        //Some initial calculations to determine the scale of the city, as well
        //as the number of things that populate it. The minimum size of the city
        //is predetermined, so as to allow enough room for things to populate it.
        int boxWidth = (int) Math.floor(Math.random()*15) + 5;
        int boxLength = (int) Math.floor(Math.random()*15) + 5;
        
        City theBox = new City();
        
        //As I'm sure you can see, the number of things scales with the size of
        //the city. About 20% of the city's intersections should be covered.
        int numOfThings = (int) (0.2*(boxWidth * boxLength)) + 5;
        
        //Initializing the thing coordinate variables. You'll notice I have the
        //Y and X coordinates reversed throughout this document. I've always
        //struggled with and been irritated by the robot API referring to it as
        //'streets' and 'avenues', and when I started coding like this, it
        //worked. So I kept the schema.
        int thingY = 0;
        int thingX = 0;
        
        //An array to hold all of the things.
        Thing allThings[] = new Thing[numOfThings + 1];
        
        //Randomly generated, randomly placed. There is a possibility, though
        //astronomically small, that all of the things will be placed in one
        //spot. I expect you're not going to run the program the (1/25)^10 times
        //it would take to likely see that scenario even in the smallest 5x5 grid.
        for (int i = 0; i < numOfThings; i++) {
            thingY = (int) Math.floor(Math.random()*boxLength) + 1;
            thingX = (int) Math.floor(Math.random()*boxWidth) + 1;
            
            allThings[i] = new Thing(theBox, thingY, thingX);            
        }
        
        //And finally, one last thing placed to guarantee that there is at least
        //one double parked intersection, using the coordinates of the previous
        //one.
        allThings[numOfThings] = new Thing(theBox, thingY, thingX);
        
        //The robot's randomized creation inside theBox.
        int jacksonY = (int) Math.floor(Math.random()*boxLength) + 1;
        int jacksonX = (int) Math.floor(Math.random()*boxWidth) + 1;
        
        //I couldn't find a method to randomize direction, so I used a switch
        //case and normal random number to determine which direction the robot
        //starts in.
        int initDirection = (int) Math.floor(Math.random()*4) + 1;
        
        //I would have appreciated hearing about enums in some class or another,
        //had to do that one entirely on my own.
        Direction jacksonDir = Direction.NORTH;
        switch (initDirection) {
            case 1:
                jacksonDir = Direction.NORTH;
                break;
            case 2:
                jacksonDir = Direction.WEST;
                break;
            case 3:
                jacksonDir = Direction.EAST;
                break;
            case 4:
                jacksonDir = Direction.SOUTH;
                break;
        }
        
        //Completely randomized.
        Robot jackson = new Robot(theBox, jacksonY, jacksonX, jacksonDir);
        
        //Obvious enough. The reason I put this one into a separate method and
        //not the others is because there were enough variables being crossed
        //over between the others, and I didn't want to deal with importing each
        //one into the methods.
        makeWalls(theBox, boxWidth, boxLength);
        
        //Just point 'im in the right direction once the program starts.
        jacksonDir = jackson.getDirection();
        
        if (jacksonDir == Direction.WEST)
            turnRight(jackson);
        else if (jacksonDir == Direction.EAST)
            jackson.turnLeft();
        else if (jacksonDir == Direction.SOUTH)
            turnAround(jackson);
        
        //Once he's facing north, it's easy to get him to the upper left corner.
        while (jackson.frontIsClear())
            jackson.move();
        jackson.turnLeft();
        while (jackson.frontIsClear())
            jackson.move();
        jackson.turnLeft();
        
        //Position corrected. The robot is in the upper left corner, facing 
        //south.
        
        //Calling up the parameters object to start telling the robot where not
        //to go.
        RobotParameters jacksonPar = new RobotParameters();
        
        //I wish I could have found a method to complete both these functions at
        //once, but as far as I know, a method can only return one variable. So
        //I'd have to write each of these as a method individually, which ends
        //up being more trouble than it's worth.
        jacksonY = jackson.getStreet();
        jacksonX = jackson.getAvenue();
        
        //Again, these variables in particular don't do anything, but I kept 
        //them for the sake of any updates.
        jacksonPar.cornerNW[0] = jacksonY;
        jacksonPar.cornerNW[1] = jacksonX;
        
        //The circuit counter may not be entirely necessary either, as it 
        //becomes irrelevant beyond the first completed concentric square, but
        //I kept it in to possibly aid with the same calculations that would
        //involve the original box's corners.
        //The turn count is absolutely necessary. It functions as the direction
        //sensor without actually using getDirection.
        int circuitCounter = 0;
        int turnCount = 0;
        
        //Program never ends until it reaches the final break.
        while (true) {
            
            //The steps the robot takes while still running alongside the walls
            //are different than in the concentric squares, so I run this part
            //of the program in a separate loop.
            while (circuitCounter < 1) {
            
                //The answer to all your thing problems. Pick up as many as you
                //can on each step!
                while (jackson.canPickThing())
                    jackson.pickThing();
                
                //The end condition for the first square tests first, to prevent
                //it from possibly moving further and crashing.
                if (jackson.frontIsClear()) {
                    //turnCount of 3 means it's facing west.
                    if (turnCount == 3) {
                        jacksonX = jackson.getAvenue();
                        //Once it reaches the intersection immediately before
                        //the northwest corner, meaning the circuit is complete,
                        //it turns and begins the concentric squares, never
                        //running over its path.
                        if (jacksonX == (jacksonPar.cornerNW[1] + 1)) {
                            circuitCounter++;
                            jackson.turnLeft();
                            jacksonY = jackson.getStreet();
                            //The aforementioned moving corners. I suppose that
                            //technically only one coordinate is needed for each
                            //corner in terms of current calculations, but again,
                            //I wanted to keep the possibility for expanded
                            //functionality.
                            jacksonPar.movingCornerNW[0] = jacksonY;
                            jacksonPar.movingCornerNW[1] = jacksonX;
                            //turnCount resets to 0, for facing south.
                            turnCount = 0;
                        }
                        //In case it DOES happen to be on the top border of the
                        //box, but not turning left yet.
                        else
                            jackson.move();
                    }
                    //The robot moving with the front clear anywhere else on
                    //the outer box.
                    else
                        jackson.move();
                }
                    
                //The robot is forced to make a turn somewhere.
                else {
                    jackson.turnLeft();
                    turnCount++;
                    
                    //For each corner, sets both the city's corner and the start
                    //of the moving corners.
                    if (turnCount == 1) {
                        jacksonY = jackson.getStreet();
                        jacksonX = jackson.getAvenue();
                        jacksonPar.cornerSW[0] = jacksonY;
                        jacksonPar.cornerSW[1] = jacksonX;
                        
                        jacksonPar.movingCornerSW[0] = jacksonY;
                        jacksonPar.movingCornerSW[1] = jacksonX;
                    }
                    else if (turnCount == 2) {
                        jacksonY = jackson.getStreet();
                        jacksonX = jackson.getAvenue();
                        jacksonPar.cornerSE[0] = jacksonY;
                        jacksonPar.cornerSE[1] = jacksonX;
                        
                        jacksonPar.movingCornerSE[0] = jacksonY;
                        jacksonPar.movingCornerSE[1] = jacksonX;
                    }
                    else if (turnCount == 3) {
                        jacksonY = jackson.getStreet();
                        jacksonX = jackson.getAvenue();
                        jacksonPar.cornerNE[0] = jacksonY;
                        jacksonPar.cornerNE[1] = jacksonX;
                        
                        jacksonPar.movingCornerNE[0] = jacksonY;
                        jacksonPar.movingCornerNE[1] = jacksonX;
                    }
                       
                }
            }
        
            //The beginning of the second phase, when the robot is no longer
            //exploring the boundaries of the city, and is cleaning up the 
            //streets (go Robocop!) in concentric squares. Or rectangles.
            while (circuitCounter >= 1){
                
                //Same as before.
                while (jackson.canPickThing())
                    jackson.pickThing();
                
                //The final break condition, meant to stop the robot after it 
                //realizes that the moving corners are on the same spot.
                if ((jacksonPar.movingCornerSW[0] == jacksonPar.movingCornerNE[0]
                        || jacksonPar.movingCornerSE[1] == jacksonPar.movingCornerNW[1])) {
                    victory(jackson);
                    break;
                }                                    
                
                //Basically, it uses the turnCount to judge what corner it should
                //be anticipating, and after every step, tests its current
                //location to figure out if it's one avenue and one street away
                //from said corner. If so, it turns, and turnCounter goes up.
                jacksonY = jackson.getStreet();
                jacksonX = jackson.getAvenue();
                if ((turnCount == 0) && (jacksonY == 
                        (jacksonPar.movingCornerSW[0] - 1))) {
                    jackson.turnLeft();
                    jacksonPar.movingCornerSW[0] = jacksonY;
                    jacksonPar.movingCornerSW[1] = jacksonX;
                    turnCount++;    
                }
                
                else if ((turnCount == 1) && (jacksonX == 
                        (jacksonPar.movingCornerSE[1] - 1))) {
                    jackson.turnLeft();
                    jacksonPar.movingCornerSE[0] = jacksonY;
                    jacksonPar.movingCornerSE[1] = jacksonX;
                    turnCount++;
                }
                
                else if ((turnCount == 2) && (jacksonY ==
                        (jacksonPar.movingCornerNE[0] + 1))) {
                    jackson.turnLeft();
                    jacksonPar.movingCornerNE[0] = jacksonY;
                    jacksonPar.movingCornerNE[1] = jacksonX;
                    turnCount++;
                }
                
                //The final turn completes a circuit, and the circuitCounter 
                //goes up, a measure of how many streets or avenues the robot is
                //from the walls of the box.
                else if ((turnCount == 3) && (jacksonX ==
                        (jacksonPar.movingCornerNW[1] + 1))) {
                    jackson.turnLeft();
                    jacksonPar.movingCornerNW[0] = jacksonY;
                    jacksonPar.movingCornerNW[1] = jacksonX;
                    turnCount = 0;
                    circuitCounter++;
                }
                
                //If there's no need to turn, move on ahead.
                else
                    jackson.move();
            }
            
        }
        
    }
    
    //Take the randomly generated city parameters, make x walls on the north and
    //south faces, and y walls on the west and east faces. Nothing complicated.
    private static void makeWalls(City city, int width, int length) {
        
        Wall[] walls = new Wall[2*(width + length)];
        for (int i = 0; i < width; i++)
            walls[i] = new Wall(city, 1, 1+i, Direction.NORTH);
        for (int i = 0; i < width; i++)
            walls[i + width] = new Wall(city, length, 1+i, Direction.SOUTH);
        for (int i = 0; i < length; i++)
            walls[i + (2*width)] = new Wall(city, 1+i, 1, Direction.WEST);
        for (int i = 0; i < length; i++)
            walls[i + length + (2*width)] = new Wall(city, 1+i, width, Direction.EAST);
        
    }
    
    //These methods aren't inherently useful given the robot's path. I only use
    //them to save space during reorientation. They -could- be used more later
    //if the robot ever makes it out of the center of his squares.
    private static void turnAround(Robot robot) {
        for (int i=0; i<2; i++)
            robot.turnLeft();
    }
    
    private static void turnRight(Robot robot) {
        for (int i=0; i<3; i++)
            robot.turnLeft();
    }
    
    private static void victory (Robot robot) {
        try {
            Thread.sleep(3000);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        for (int i = 0; i<20; i++)
            robot.turnLeft();
        
        while (true) {
            if (robot.getDirection()==Direction.NORTH) {
                JOptionPane.showMessageDialog(null, "Ta-da!", "VICTORY", 
                JOptionPane.WARNING_MESSAGE);
                break;
            }
            robot.turnLeft();
        }
    }
    
}