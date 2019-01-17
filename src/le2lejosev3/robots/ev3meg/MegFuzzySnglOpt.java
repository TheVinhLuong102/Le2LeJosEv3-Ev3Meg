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
import lejos.hardware.port.SensorPort;

/**
 * Ev3Meg Robot (optimized single thread version)
 * EV3MEG is a small helper robot that can drive and follow a black line on a
 * light surface. It uses a smart fuzzy logic to drive.
 * 
 * @author Roland Blochberger
 */
public class MegFuzzySnglOpt {

	private static Class<?> clazz = MegFuzzySnglOpt.class;
	private static final Logger log = Logger.getLogger(clazz.getName());

	// the global variables
	// Speed (nominal)
	static float speed = 0F;
	// Light reflected light intensity
	static int light = 0;
	// Dark reflected light intensity
	static int dark = 0;

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
		// debug
		log.info("speed: " + speed);

		// the medium motor
		MediumMotor motA = new MediumMotor(MotorPort.A);
		// move tank block with both large motors
		MoveTank move = new MoveTank(MotorPort.B, MotorPort.C);
		// the sensors
		ColorSensor color = new ColorSensor(SensorPort.S3);
		InfraredSensor infra = new InfraredSensor(SensorPort.S4);
		// debug (the instantiations take about 9 seconds!)
		log.info("init motors & sensors done");

		// initialize the grippers
		// Medium Motor A on with power -11
		motA.motorOn(-11);
		// Wait 1 second
		Wait.time(1F);
		// Medium Motor A off with brake at end
		motA.motorOff(true);

		// Setup light reflected light intensity
		light = measureIntensity(color, true);

		// Setup dark reflected light intensity
		dark = measureIntensity(color, false);

		// Display Text "Put on right edge" on Pixel coordinates 0,30 with color black
		// (false) and bold font (1) and clear screen before
		Display.textPixels("Put on right edge", true, 0, 30, Display.COLOR_BLACK, Display.FONT_BOLD);
		// Display Text "and press center" on Pixel coordinates 0,50 with color black
		// (false) and bold font (1) and no clear screen before
		Display.textPixels("and press center", false, 0, 50, Display.COLOR_BLACK, Display.FONT_BOLD);
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

		log.info("GO");
		// Display "GO" on Pixel coordinates 50,30 with color black (false) and large
		// font (2) and clear screen before
		// Display.textPixels("GO", true, 50, 30, Display.COLOR_BLACK,
		// Display.FONT_LARGE);

		// Display Light value on Pixel coordinates 10,30 with color black (false) and
		// large font (2) and clear screen before
		Display.textPixels(light, true, 10, 30, Display.COLOR_BLACK, Display.FONT_LARGE);
		// Display Dark value on Pixel coordinates 10,30 with color black (false) and
		// large font (2) and no clear screen before
		Display.textPixels(dark, false, 10, 90, Display.COLOR_BLACK, Display.FONT_LARGE);

		// main Loop
		int cnt = 0;
		int grey = 0;
		int ogrey = 0;
		int drive_b = 0, drive_c = 0;
		int dif = 0;
		float spd = 0F;
		// long stim = 0;
		// long peri = 0;
		while (Button.ESCAPE.isUp()) {

			cnt++;
			if (cnt >= 5) {
				// do every 5th run:
				cnt = 0;
				// compare Infrared sensor proximity with 40
				if (infra.measureProximity() < 40) {
					// obstacle detected:
					// move grippers and turn around
					obstacle(move, motA, color);
					// continue with loop
					continue;
				}
			}

			// follow the line:
			// get start timestamp
			// stim = System.currentTimeMillis();

			// measure reflected light intensity
			grey = color.measureReflectedLightIntensity();
			// Display grey value on Pixel coordinates 10,60 with color black (false) and
			// large font (2) and no clear screen before
			Display.textPixels(grey + " ", false, 10, 60, Display.COLOR_BLACK, Display.FONT_LARGE);

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

			// calculate power for motor B (left) - turns from dark to light
			drive_b = Math.round((grey - dark) * spd);
			// calculate power for motor C (right) - turns from light to dark
			drive_c = Math.round((light - grey) * spd);
			// Move tank with the calculated power values
			move.motorsOn(drive_b, drive_c);

			// wait at least 15ms until next sample, periods are up to 30ms(!)
			// while ((peri = (System.currentTimeMillis() - stim)) < 15L) {
			// Thread.yield();
			// }
			// debug
			// log.info("grey: " + grey + ", dif: " + dif + ", spd: " + spd + ", peri: " +
			// peri);
		}

		log.info("The End");
	}

	/**
	 * Setup light or dark reflected light intensity.
	 * 
	 * @param color       the ColorSensor instance.
	 * @param lightIntens set true for light reflected light intensity; false for
	 *                    dark reflected light intensity.
	 * @return the reflected color value.
	 */
	private static int measureIntensity(ColorSensor color, boolean lightIntens) {
		// Display Text "Initiate" on Pixel coordinates 0,0 with color black (false) and
		// large font (2) and clear screen before
		Display.textPixels("Initiate", true, 0, 0, Display.COLOR_BLACK, Display.FONT_LARGE);
		// Display Text "Put on light/dark colour" on Pixel coordinates 0,30 with color
		// black (false) and bold font (1) and no clear screen before
		Display.textPixels("Put on " + (lightIntens ? "light" : "dark") + " colour", false, 0, 30, Display.COLOR_BLACK,
				Display.FONT_BOLD);
		// Display Text "and press center" on Pixel coordinates 0,50 with color black
		// (false) and bold font (1) and no clear screen before
		Display.textPixels("and press center", false, 0, 50, Display.COLOR_BLACK, Display.FONT_BOLD);
		// Wait until Brick button 2 pressed
		while (Button.ESCAPE.isUp()) {
			if (BrickButtons.measure() == BrickButtons.BB_CENTER) {
				break;
			}
			// wait between samples
			Wait.time(0.01F);
		}

		// get reflected light intensity
		int intens = color.measureReflectedLightIntensity();
		log.info("intens: " + intens);
		// Sound play file "Detected" with volume 100 and wait until done (0)
		Sound.playFile("Detected", 100, Sound.WAIT);
		// Sound play file "White"/"Black" with volume 100 and wait until done (0)
		Sound.playFile((lightIntens ? "White" : "Black"), 100, Sound.WAIT);
		// Wait until no Brick button pressed
		while (Button.ESCAPE.isUp()) {
			if (BrickButtons.measure() == BrickButtons.BB_NONE) {
				break;
			}
			// wait between samples
			Wait.time(0.01F);
		}
		return intens;
	}

	/**
	 * handle obstacle: move grippers and turn around.
	 * 
	 * @param move  the MoveTank instance.
	 * @param motA  the Medium Motor instance.
	 * @param color the ColorSensor instance.
	 */
	private static void obstacle(MoveTank move, MediumMotor motA, ColorSensor color) {
		// obstacle detected:
		log.info("Start");
		// Move tank off and brake at end
		move.motorsOff(true);
		// Display "OBSTACLE" on Pixel coordinates 20,30 with color black (false) and
		// large font (2) and clear screen before
		// Display.textPixels("OBSTACLE", true, 20, 30, Display.COLOR_BLACK,
		// Display.FONT_LARGE);
		// Sound play file "Stop" with volume 100 and wait until done (0)
		Sound.playFile("Stop", 100, Sound.WAIT);

		// move grippers
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

		// turn backwards on the spot
		// Sound play file "Backwards" with volume 100 and wait until done (0)
		Sound.playFile("Backwards", 100, Sound.WAIT);
		// calc. color threshhold
		int colThre = Math.round((light - dark) / 2F);
		// reset left motor rotation
		move.rotationResetLeft();
		// Move tank on with power 50, -50 for 0.3 rotation and do not brake at end
		move.motorsOnForRotations(50, -50, 0.3F, false);
		// find black line again, however do not rotate more than 5 times
		while (Button.ESCAPE.isUp() && (move.measureRotationsLeft() < 5F)
				&& (color.measureReflectedLightIntensity() > colThre)) {
			// rotate slowly, but fast enough to stop only after the right edge of the line
			move.motorsOn(38, -38);
			// wait between samples
			Wait.time(0.003F);
		}
		// Move tank stop with brake
		move.motorsOff(true);
		// debug
		log.info("rotations: " + move.measureRotationsLeft());

		// Wait 1 second
		Wait.time(1F);

		// Display "GO" on Pixel coordinates 40,30 with color black (false) and large
		// font (2) and clear screen before
		// Display.textPixels("GO", true, 50, 30, Display.COLOR_BLACK,
		// Display.FONT_LARGE);
		// Sound play file "Start" with volume 100 and wait until done (0)
		Sound.playFile("Start", 100, Sound.WAIT);

		log.info("End");
	}
}
