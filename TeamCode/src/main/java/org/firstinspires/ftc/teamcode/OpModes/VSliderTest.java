package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Helper.Robot;

@TeleOp(name = "VSlider Test", group = "LinearOpMode")

public class VSliderTest extends LinearOpMode {
    //tells you how long the robot has run for
    private ElapsedTime runtime = new ElapsedTime();
    double timeout_ms = 0;

    Robot robot = new Robot();

    @Override
    public void runOpMode() throws InterruptedException {
        /**
         * Instance of Robot class is initalized
         */
        robot.init(hardwareMap);

        /**
         * This code is run during the init phase, and when opMode is not active
         * i.e. When "INIT" Button is pressed on the Driver Station App
         */

        waitForStart();



        while (opModeIsActive()) {


            /**
             * Joystick controls for slider
             */

            double vSliderPower =  gamepad2.left_stick_y;
            robot.vSlider.setPower(vSliderPower);

//
//            if(gamepad2.a) {
//                robot.MoveSlider(1, 1000);
//            }
//            if(gamepad2.b) {
//                robot.MoveSlider(1, -1000);
//            }

            telemetry.addData("vSliderPower", vSliderPower);
            telemetry.addData("vSlider Encoder", robot.vSlider.getCurrentPosition());
            telemetry.update();

        }

    }

}
