/**
 * 
 */
package le2lejosev3.robots.ev3meg;

import java.util.logging.Logger;

import le2lejosev3.logging.Setup;
import le2lejosev3.pblocks.BrickButtons;
import le2lejosev3.pblocks.ColorSensor;
import le2lejosev3.pblocks.Display;
import le2lejosev3.pblocks.InfraredSensor;
import le2lejosev3.pblocks.MediumMotor;
import le2lejosev3.pblocks.MoveTank;
import le2lejosev3.pblocks.Sound;
import le2lejosev3.pblocks.Wait;
import lejos.hardware.Button;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;

/**
 * Ev3Meg Robot (optimized Java port of original icon-based program)
 * EV3MEG is a small helper robot that can drive and follow a black line on a
 * light surface. It uses a smart fuzzy logic to drive.
 * 
 * @author Roland Blochberger
 */
public class MegFuzzyOpt {

	private static Class<?> clazz = MegFuzzyOpt.class;
	private static final Logger log = Logger.getLogger(clazz.getName());

	// the robot configuration
	static final Port motorPortA = MotorPort.A; // Medium Motor
	static final Port motorPortB = MotorPort.B; // Large Motor
	static final Port motorPortC = MotorPort.C; // Large Motor
	static final Port colorPort3 = SensorPort.S3; // Color Sensor
	static final Port infraPort4 = SensorPort.S4; // Infrared Sensor

	// the medium motor
	static final MediumMotor motA = new MediumMotor(motorPortA);
	// move tank block with both large motors
	private static final MoveTank move = new MoveTank(motorPortB, motorPortC);
	// the sensors
	private static final ColorSensor color = new ColorSensor(colorPort3);
	private static final InfraredSensor infra = new InfraredSensor(infraPort4);

	// the variables
	// (single variables need no synchronisation between threads)
	// Go
	static boolean go = false;
	// NoGo (inverted from the original icon-based program)
	static boolean nogo = false;
	// Speed
	static float speed = 0F;
	// Drive_C
	static int drive_c = 0;
	// Drive_B
	static int drive_b = 0;
	// Light
	static int light = 0;
	// Dark
	static int dark = 0;
	// grey
	static int grey = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// setup logging to file for all levels
		// Setup.log2File(clazz, Level.ALL);
		// setup logging to file
		Setup.log2File(clazz);
		log.info("Starting ...");

		// Init Loop
		// Set Speed to -1.4 (this works fairly well in the LEGO icon-based program)
		// speed = -1.4F;
		// Set Speed for Java :-(
		speed = -0.4F;
		// reset NoGo and Go
		nogo = false;
		go = false;

		// Create and start the threads
		Thread cdth = new ColorDetectionThread();
		cdth.start();
		Thread spth = new SpeedThread();
		spth.start();
		Thread drth = new DriveThread();
		drth.start();
		Thread odth = new ObjectDetectionThread();
		odth.start();

		// Medium Motor A on with power -11
		motA.motorOn(-11);
		// Wait 1 second
		Wait.time(1F);
		// Medium Motor A off with brake at end
		motA.motorOff(true);

		// Display Text "Initiate" on Pixel coordinates 0,0 with color black (false) and
		// large font (2) and clear screen before
		Display.textPixels("Initiate", true, 0, 0, Display.COLOR_BLACK, Display.FONT_LARGE);
		// Display Text "Put on light colour" on Pixel coordinates 0,30 with color black
		// (false) and bold font (1) and no clear screen before
		Display.textPixels("Put on light colour", false, 0, 30, Display.COLOR_BLACK, Display.FONT_BOLD);
		// Display Text "and press..." on Pixel coordinates 0,50 with color black
		// (false) and bold font (1) and no clear screen before
		Display.textPixels("and press...", false, 0, 50, Display.COLOR_BLACK, Display.FONT_BOLD);
		// Wait until Brick button 2 pressed
		while (Button.ESCAPE.isUp()) {
			if (BrickButtons.measure() == BrickButtons.BB_CENTER) {
				break;
			}
			// wait between samples
			Wait.time(0.02F);
		}

		// Store reflected light intensity
		light = color.measureReflectedLightIntensity();
		// Sound play file "Detected" with volume 100 and wait until done (0)
		Sound.playFile("Detected", 100, Sound.WAIT);
		// Sound play file "White" with volume 100 and wait until done (0)
		Sound.playFile("White", 100, Sound.WAIT);
		// Wait until no Brick button pressed
		while (Button.ESCAPE.isUp()) {
			if (BrickButtons.measure() == BrickButtons.BB_NONE) {
				break;
			}
			// wait between samples
			Wait.time(0.02F);
		}

		// Display Text "Initiate" on Pixel coordinates 0,0 with color black (false) and
		// large font (2) and clear screen before
		Display.textPixels("Initiate", true, 0, 0, Display.COLOR_BLACK, Display.FONT_LARGE);
		// Display Text "Put on dark colour" on Pixel coordinates 0,30 with color black
		// (false) and bold font (1) and no clear screen before
		Display.textPixels("Put on dark colour", false, 0, 30, Display.COLOR_BLACK, Display.FONT_BOLD);
		// Display Text "and press..." on Pixel coordinates 0,50 with color black
		// (false) and bold font (1) and no clear screen before
		Display.textPixels("and press...", false, 0, 50, Display.COLOR_BLACK, Display.FONT_BOLD);
		// Wait until Brick button 2 pressed
		while (Button.ESCAPE.isUp()) {
			if (BrickButtons.measure() == BrickButtons.BB_CENTER) {
				break;
			}
			// wait between samples
			Wait.time(0.01F);
		}
		// Store reflected light intensity
		dark = color.measureReflectedLightIntensity();
		// Sound play file "Detected" with volume 100 and wait until done (0)
		Sound.playFile("Detected", 100, Sound.WAIT);
		// Sound play file "Black" with volume 100 and wait until done (0)
		Sound.playFile("Black", 100, Sound.WAIT);
		// Wait until no Brick button pressed
		while (Button.ESCAPE.isUp()) {
			if (BrickButtons.measure() == BrickButtons.BB_NONE) {
				break;
			}
			// wait between samples
			Wait.time(0.01F);
		}

		// Display Text "Press Start" on Pixel coordinates 0,50 with color black (false)
		// and large font (2) and clear screen before
		Display.textPixels("Press Start", true, 0, 50, Display.COLOR_BLACK, Display.FONT_LARGE);
		// Sound play file "Start" with volume 100 and wait until done (0)
		Sound.playFile("Start", 100, Sound.WAIT);

		// Wait until Brick button 2 pressed
		while (Button.ESCAPE.isUp()) {
			if (BrickButtons.measure() == BrickButtons.BB_CENTER) {
				break;
			}
			// wait between samples
			Wait.time(0.01F);
		}

		// let the other threads go
		go = true;
		log.info("GO");
	}

	/**
	 * main color detection loop; wait for init to complete first.
	 */
	static class ColorDetectionThread extends Thread {

		/**
		 * Constructor.
		 */
		public ColorDetectionThread() {
			// set as daemon thread
			setDaemon(true);
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			// Wait 0.5 seconds
			Wait.time(0.5F);
			// Init Loop
			while (Button.ESCAPE.isUp()) {
				// Wait until init completed
				if (go) {
					break;
				}
				// wait between samples
				Wait.time(0.01F);
			}
			log.info("GO");

			// Display Light value on Pixel coordinates 10,30 with color black (false) and
			// large font (2) and no clear screen before
			Display.textPixels(light, true, 10, 30, Display.COLOR_BLACK, Display.FONT_LARGE);
			// Display Dark value on Pixel coordinates 10,30 with color black (false) and
			// large font (2) and no clear screen before
			Display.textPixels(dark, false, 10, 90, Display.COLOR_BLACK, Display.FONT_LARGE);

			// Light / Screen Loop
			int grey = 0;
			while (Button.ESCAPE.isUp()) {
				// measure reflected light intensity
				grey = color.measureReflectedLightIntensity();
				synchronized (MegFuzzyOpt.class) {
					// export to other threads
					MegFuzzyOpt.grey = grey;
				}
				// Display grey value on Pixel coordinates 10,60 with color black (false) and
				// large font (2) and clear screen before
				Display.textPixels(grey, false, 10, 60, Display.COLOR_BLACK, Display.FONT_LARGE);
				// wait between samples
				// Wait.time(0.003F);
			}
		}
	}

	/**
	 * main speed loop; calculate the drive speed of the two motors; wait for init
	 * to complete first.
	 */
	static class SpeedThread extends Thread {

		/**
		 * Constructor.
		 */
		public SpeedThread() {
			// set as daemon thread
			setDaemon(true);
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			// Wait 0.5 seconds
			Wait.time(0.5F);
			// Init Loop
			while (Button.ESCAPE.isUp()) {
				// Wait until init completed
				if (go) {
					break;
				}
				// wait between samples
				Wait.time(0.01F);
			}
			log.info("GO");

			// Light / Screen Loop
			int grey = 0;
			int ogrey = 0;
			int drive_c = 0, drive_b = 0;
			int dif = 0;
			float spd = 0F;
			while (Button.ESCAPE.isUp()) {
				synchronized (MegFuzzyOpt.class) {
					// import from other thread
					grey = MegFuzzyOpt.grey;
				}

				// try to optimize the speed:
				// use more speed if grey differences are small and less speed if differences
				// are big
				dif = Math.abs(grey - ogrey);
				ogrey = grey;
				// limit the difference
				if (dif > 30) {
					dif = 30;
				}
				// allow speed range -0.4 * (0.75 .. 1.25) = -0.3 .. -0.5
				spd = speed * Math.abs(1.25F - (dif / 60F));

				// calculate power for motor C
				drive_c = Math.round((light - grey) * spd);
				// calculate power for motor B
				drive_b = Math.round((grey - dark) * spd);

				synchronized (MegFuzzyOpt.class) {
					// export to other thread
					MegFuzzyOpt.drive_c = drive_c;
					MegFuzzyOpt.drive_b = drive_b;
				}
				// wait between calculations
				Wait.time(0.002F);
			}
		}
	}

	/**
	 * main drive loop; only one place to control all motor behaviour; wait for init
	 * to complete first.
	 * (this is no daemon thread, if it ends the whole program ends as well)
	 */
	static class DriveThread extends Thread {

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			// Wait 0.5 seconds
			Wait.time(0.5F);
			// Init Loop
			while (Button.ESCAPE.isUp()) {
				// Wait until init completed
				if (go) {
					break;
				}
				// wait between samples
				Wait.time(0.01F);
			}
			log.info("GO");

			// calc. color threshhold
			int colThre = Math.round((light - dark) / 2F);
			// Drive Loop
			int drive_c, drive_b;
			while (Button.ESCAPE.isUp()) {
				// check NoGo variable
				if (nogo) {
					// obstacle detected:
					log.info("NOGO");
					// Move tank off and brake at end
					move.motorsOff(true);
					// Sound play file "Stop" with volume 100 and wait until done (0)
					Sound.playFile("Stop", 100, Sound.WAIT);

					// Motor A on with power 21
					motA.motorOn(21);
					// Wait 1 second
					Wait.time(1F);
					// Motor A on with power -21
					motA.motorOn(-21);
					// Wait 1 second
					Wait.time(1F);
					// Motor A off and brake at end
					motA.motorOff(true);

					// Sound play file "Backwards" with volume 100 and wait until done (0)
					Sound.playFile("Backwards", 100, Sound.WAIT);

					// reset left motor rotation
					move.rotationResetLeft();
					// Move tank on with power 50, -50 for 0.3 rotation and do not brake at end
					move.motorsOnForRotations(50, -50, 0.3F, false);
					// rotate slowly, but fast enough to stop only after the right edge of the line
					move.motorsOn(38, -38);
					// find black line again, however do not rotate more than 5 times
					while (Button.ESCAPE.isUp() && (move.measureRotationsLeft() < 5F)
							&& (color.measureReflectedLightIntensity() > colThre)) {
						// wait between samples
						Wait.time(0.003F);
					}
					// Move tank stop with brake
					move.motorsOff(true);
					// debug
					log.info("rotations: " + move.measureRotationsLeft());
					// Wait 1 second
					Wait.time(1F);

					// Sound play file "Start" with volume 100 and wait until done (0)
					Sound.playFile("Start", 100, Sound.WAIT);
					// reset NoGo variable
					nogo = false;
					log.info("GO");

				} else {
					// no obstacle detected:
					synchronized (MegFuzzyOpt.class) {
						// import from other thread
						drive_c = MegFuzzyOpt.drive_c;
						drive_b = MegFuzzyOpt.drive_b;
					}
					// Move tank with the calculated power values
					move.motorsOn(drive_b, drive_c);
				}
				// wait between loops
				Wait.time(0.003F);
			}
		}
	}

	/**
	 * main object detection loop; wait for init to complete first.
	 */
	static class ObjectDetectionThread extends Thread {

		/**
		 * Constructor.
		 */
		public ObjectDetectionThread() {
			// set as daemon thread
			setDaemon(true);
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			// Wait 0.5 seconds
			Wait.time(0.5F);
			// Init Loop
			while (Button.ESCAPE.isUp()) {
				// Wait until init completed
				if (go) {
					break;
				}
				// wait between samples
				Wait.time(0.01F);
			}
			log.info("GO");

			// Wall Loop
			while (Button.ESCAPE.isUp()) {
				// compare Infrared sensor proximity with 40
				if (!nogo && infra.measureProximity() < 40) {
					// set NoGo variable
					nogo = true;
				}
				// wait between samples
				Wait.time(0.01F);
			}
		}
	}
}
