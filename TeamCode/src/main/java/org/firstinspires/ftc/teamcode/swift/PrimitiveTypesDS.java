package org.firstinspires.ftc.teamcode.swift;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
 @TeleOp()
 public class PrimitiveTypesDS extends OpMode {
 @Override
 public void init() {
 int teamNumber = 17348;
 double motorSpeed = 0.5;
 boolean touchSensorPressed = true;

telemetry.addData("Hello","I'm Dawston");
telemetry.addData("Team Number", teamNumber);
telemetry.addData("Motor Speed", motorSpeed);
telemetry.addData("Touch Sensor", touchSensorPressed);
 }

@Override
 public void loop() {

  }
 }