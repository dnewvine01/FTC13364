/*
 * Copyright (c) 2021 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.firstinspires.ftc.teamcode.opmodes.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.vision.SleeveDetector;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;

@Autonomous(name="Autonomous :)")
public class Autonomous_test extends LinearOpMode
{
    private DcMotor motorFL = null;
    private DcMotor motorFR = null;
    private DcMotor motorBL = null;
    private DcMotor motorBR = null;
    private DcMotor leftLift = null;
    private DcMotor rightLift = null;
    private Servo gripper = null;
    private ElapsedTime timeElapsed = new ElapsedTime();

    double fx = 578.272;
    double fy = 578.272;
    double cx = 402.145;
    double cy = 221.506;
    double tagsize = 0.166;

    int LEFT = 1;
    int MIDDLE = 2;
    int RIGHT = 3;

    AprilTagDetection tagOfInterest = null;

    OpenCvCamera camera;
    SleeveDetector aprilTagDetectionPipeline;

    @Override
    public void runOpMode()
    {
        motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        leftLift = hardwareMap.get(DcMotor.class, "leftArm");
        rightLift = hardwareMap.get(DcMotor.class, "rightArm");
        gripper = hardwareMap.get(Servo.class, "gripper");

        motorBR.setDirection(DcMotor.Direction.REVERSE);
        motorFR.setDirection(DcMotor.Direction.REVERSE);
        leftLift.setDirection(DcMotor.Direction.REVERSE);

        //resetting encoders at home level
        leftLift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightLift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        aprilTagDetectionPipeline = new SleeveDetector(tagsize, fx, fy, cx, cy);

        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                camera.startStreaming(800,448, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode)
            {

            }
        });

        telemetry.addLine("waiting to start!");
        telemetry.update();

        while (!isStarted() && !isStopRequested())
        {
            ArrayList<AprilTagDetection> currentDetections = aprilTagDetectionPipeline.getLatestDetections();

            for(AprilTagDetection tag : currentDetections)
            {
                if(tag.id == LEFT || tag.id == MIDDLE || tag.id == RIGHT)
                {
                    tagOfInterest = tag;
                    break;
                }
            }

            if(tagOfInterest != null) telemetry.addLine(String.format("\nDetected tag ID=%d", tagOfInterest.id));
            else telemetry.addLine("Don't see tag of interest :(");

            telemetry.update();
            sleep(20);
        }

        //waitForStart();

        boolean parked = false;

        while(opModeIsActive() && !parked) {
            runToPosition(-100, -100, -100, -100);

            gripper.setPosition(0.53);

            leftLift.setTargetPosition(-200);
            rightLift.setTargetPosition(-200);
            leftLift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightLift.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            while (leftLift.isBusy() || rightLift.isBusy()) {
                if ((leftLift.getCurrentPosition() + rightLift.getCurrentPosition())/2 > (leftLift.getTargetPosition() + rightLift.getTargetPosition())/2) {
                    leftLift.setPower(1.0);
                    rightLift.setPower(1.0);
                } else {
                    leftLift.setPower(0.0);
                }
            }

            motorFL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motorFR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motorBL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motorBR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            if (tagOfInterest.id == LEFT) runToPosition(1050, -1350, -1350, 1050);
            else if (tagOfInterest.id == RIGHT) runToPosition(-1350, 1050, 1050, -1350);

            motorFL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motorFR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motorBL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motorBR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            runToPosition(-1500, -1500, -1500, -1500);

            leftLift.setTargetPosition(-200);
            rightLift.setTargetPosition(-200);
            while (leftLift.isBusy() || rightLift.isBusy()) {
                if ((leftLift.getCurrentPosition() + rightLift.getCurrentPosition())/2 > (leftLift.getTargetPosition() + rightLift.getTargetPosition())/2) {
                    leftLift.setPower(1.0);
                    rightLift.setPower(1.0);
                } else {
                    leftLift.setPower(0.0);
                }
            }

            telemetry.addLine("parked!");

            parked = true;
        }
    }

    void runToPosition(int FL, int FR, int BL, int BR) {
        timeElapsed.reset();

        motorFL.setTargetPosition(FL);
        motorFR.setTargetPosition(FR);
        motorBL.setTargetPosition(BL);
        motorBR.setTargetPosition(BR);

        motorFL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorFR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorBL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorBR.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while(motorFL.isBusy() || motorFR.isBusy() || motorBL.isBusy() || motorBR.isBusy()){
            if (Math.abs(motorFL.getCurrentPosition() - FL) < 350 || Math.abs(motorFR.getCurrentPosition() - FR) < 350 || Math.abs(motorBL.getCurrentPosition() - BL) < 350 || Math.abs(motorBR.getCurrentPosition() - BR) < 350) {
                motorFL.setPower(Math.abs(motorFL.getCurrentPosition() - FL)/1000);
                motorFR.setPower(Math.abs(motorFL.getCurrentPosition() - FL)/1000);
                motorBL.setPower(Math.abs(motorFL.getCurrentPosition() - FL)/1000);
                motorBR.setPower(Math.abs(motorFL.getCurrentPosition() - FL)/1000);
            } else {
                motorFL.setPower(0.35/(1+3*Math.pow(3,-3*timeElapsed.seconds())));
                motorFR.setPower(0.35/(1+3*Math.pow(3,-3*timeElapsed.seconds())));
                motorBL.setPower(0.35/(1+3*Math.pow(3,-3*timeElapsed.seconds())));
                motorBR.setPower(0.35/(1+3*Math.pow(3,-3*timeElapsed.seconds())));
            }

            telemetry.addData("motorFL", motorFL.getCurrentPosition());
            telemetry.addData("motorFR", motorFR.getCurrentPosition());
            telemetry.addData("motorBL", motorBL.getCurrentPosition());
            telemetry.addData("motorBR", motorBR.getCurrentPosition());
            telemetry.update();
        }
    }
}