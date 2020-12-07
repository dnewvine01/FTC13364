
package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;


import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.concurrent.TimeUnit;
import java.util.Locale;


@TeleOp(name = "Graham: Mecanum", group = "Opmode")
//@Disabled
public class MecanumWheelDraft extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    GrahamHWMap robot = new GrahamHWMap();

    @Override
    public void runOpMode() {

        robot.init(hardwareMap);

        double x;
        double y;
        double r;
        double frontLeft;
        double frontRight;
        double backLeft;
        double backRight;

        double step = .1;    //was .2    //how much to update
        double interval = 25;  //was 75 // how often to update
        double lastSpeedTime = runtime.milliseconds();

        double max;

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        while (opModeIsActive()) {

            Orientation angles;

            angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);


            y = gamepad1.left_stick_y;
            x = gamepad1.left_stick_x;
            r = gamepad1.right_stick_x;


            // do not let rotation dominate movement
            r = r / 2;

            // calculate the power for each wheel
            frontLeft = +y - x + r;
            backLeft = +y + x + r;

            frontRight = -y - x + r;
            backRight = -y + x + r;
            /*
            // Normalize the values so none exceeds +/- 1.0
            max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(frontRight), Math.abs(frontRight)));
            if (max > 1.0) {
                frontLeft = frontLeft / max;
                frontRight = frontRight / max;
                backLeft = backLeft / max;
                backRight = backRight / max;
            }

             */

            if (runtime.milliseconds() > lastSpeedTime + interval) {
                lastSpeedTime = runtime.milliseconds();

                frontLeft = getRampPower(frontLeft, robot.frontLeftMotor.getPower(), step);
                frontRight = getRampPower(-frontRight, -robot.frontRightMotor.getPower(), step);
                backLeft = getRampPower(backLeft, robot.backLeftMotor.getPower(), step);
                backRight = getRampPower(-backRight, -robot.backRightMotor.getPower(), step);

                frontRight = -frontRight;
                backRight = -backRight;




                max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(frontRight), Math.abs(frontRight)));
                if (max > .9) {   //was 1
                    frontLeft = frontLeft / max;
                    frontRight = frontRight / max;
                    backLeft = backLeft / max;
                    backRight = backRight / max;
                }


                robot.frontLeftMotor.setPower(frontLeft);
                robot.frontRightMotor.setPower(frontRight);
                robot.backLeftMotor.setPower(backLeft);
                robot.backRightMotor.setPower(backRight);


                // Show wheel power to driver
                telemetry.addData("front left", "%.2f", frontLeft);
                telemetry.addData("front right", "%.2f", frontRight);
                telemetry.addData("back left", "%.2f", backLeft);
                telemetry.addData("back right", "%.2f", backRight);

                telemetry.addData("current heading", formatAngle(angles.angleUnit, angles.firstAngle));

                telemetry.update();


            }


            if (gamepad1.a){
                goToHeading(0 );

            }




        }


    }

    double getRampPower(double t, double a, double step) {
        double delta;
        double returnPower = 0;

        delta = t - a;
        if (delta > 0) {  // speeding up
            returnPower = a + step;
            if (returnPower > t) {
                returnPower = t;
            }
        }
        if (delta < 0) {  //slowing down
            returnPower = a - (step);
            if (returnPower < t)
                returnPower = t;
        }
        if (delta == 0) {
            returnPower = a;
        }
        return returnPower;
    }




    /*
    Read the starting angle and store it in a variable. Drive in a while loop with a condition on when to stop.
    Depending on the difference between the starting angle and the current angle,
    add power to one set of wheels and remove power from the other. Something like:

double error = 0.0;
double startAngle = imu.getAngle();
//or however u get ur angle while (!im there)
{ error = someConstant * (startAngle - currentAngle);
frontLeft.setPower(power+error); frontRight.setPower(power-error);
backLeft.setPower(power+error); backRight.setPower(power-error) }

Find someConstant experimentally,
it sets how sensitive the system is.
You may need to make the error negative somehow if the robot corrects the wrong way. Hope this helps!!
     */

 void goToHeading(double heading){


        double targAngle = heading;
        double error = 0;
        double sensitivityConstant = 1; //change this to change how much the robot corrects
        double local_power = .3;


        while(opModeIsActive() && (gamepad1.a)){

            Orientation currentOrient;
            currentOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            double currAngle = currentOrient.angleUnit.DEGREES.normalize(currentOrient.firstAngle);

            double raw_error = targAngle - currAngle;
            error = (sensitivityConstant * (targAngle - currAngle)) / 180;





            double frontLeft = local_power + error;
            double backLeft = local_power + error;
            double frontRight = local_power - error;
            double backRight = local_power - error;

            if (raw_error <=0){
                frontLeft = -frontLeft;
                backLeft = -backLeft;
                backRight= -backRight;
                frontRight = -frontRight;
            }



            robot.frontLeftMotor.setPower(frontLeft);
            robot.backLeftMotor.setPower(backLeft);
            robot.frontRightMotor.setPower(frontRight);
            robot.backRightMotor.setPower(backRight);

            if (targAngle == currAngle){
                robot.frontLeftMotor.setPower(0);
                robot.backLeftMotor.setPower(0);
                robot.frontRightMotor.setPower(0);
                robot.backRightMotor.setPower(0);
            }




            telemetry.addData("front left", "%.2f", local_power + error);
            telemetry.addData("back left", "%.2f", local_power + error);
            telemetry.addData("front right", "%.2f", local_power - error);
            telemetry.addData("back right", "%.2f", local_power - error);


            telemetry.addData("raw error", raw_error);
            telemetry.addData("error Output", "%.2f", error);
            telemetry.update();


        }
 }



    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees) {
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }




}