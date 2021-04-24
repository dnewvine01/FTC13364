
//written by Cooper Clem, 2019

package org.firstinspires.ftc.teamcode.robots.UGBot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.robots.UGBot.utils.Constants;
import org.firstinspires.ftc.teamcode.util.Conversions;
import org.firstinspires.ftc.teamcode.util.PIDController;

import static org.firstinspires.ftc.teamcode.util.Conversions.between360;
import static org.firstinspires.ftc.teamcode.util.Conversions.diffAngle2;
import static org.firstinspires.ftc.teamcode.util.Conversions.nextCardinal;
import static org.firstinspires.ftc.teamcode.util.Conversions.wrap360;
import static org.firstinspires.ftc.teamcode.util.Conversions.wrapAngle;
import static org.firstinspires.ftc.teamcode.util.Conversions.wrapAngleMinus;

@Config
public class Turret{
    //motor
    private  DcMotor motor = null;
    private double motorPwr = 1;
    long turnTimer;
    boolean turnTimerInit;
    private double minTurnError = 1.0;
    private boolean active = true;

    //PID
    PIDController turretPID;
    public static double kpTurret = 0.03; //proportional constant multiplier goodish
    public static  double kiTurret = .01; //integral constant multiplier
    public static  double kdTurret= .05; //derivative constant multiplier
    double correction = 0.00; //correction to apply to turret motor

    //IMU
    BNO055IMU turretIMU;
    double turretRoll;
    double turretPitch;
    double turretHeading;
    boolean initialized = false;
    private double offsetHeading;
    private double offsetRoll;
    private double offsetPitch;
    private double turretTargetHeading = 0.0;
    Orientation imuAngles;
    boolean maintainHeadingInit;
    private final double angleIncrement = 10;

    //sensors
    //DigitalChannel magSensor;


    public Turret(DcMotor motor, BNO055IMU turretIMU) {

        //motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //motor.setTargetPosition(motor.getCurrentPosition());
        //motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        //this.magSensor = magSensor;

        this.motor = motor;
        turretTargetHeading=0;
        turretPID = new PIDController(0,0,0);
        initIMU(turretIMU);
    }

    public void initIMU(BNO055IMU turretIMU){

        //setup Turret IMU
        BNO055IMU.Parameters parametersIMUTurret = new BNO055IMU.Parameters();
        parametersIMUTurret.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parametersIMUTurret.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parametersIMUTurret.loggingEnabled = true;
        parametersIMUTurret.loggingTag = "turretIMU";


        turretIMU.initialize(parametersIMUTurret);
        this.turretIMU=turretIMU;

    }

    double baseHeading = 0.0;
    public void update(double baseHeading){


        //IMU Update
        imuAngles= turretIMU.getAngularOrientation().toAxesReference(AxesReference.INTRINSIC).toAxesOrder(AxesOrder.ZYX);
        if (!initialized) {
            //first time in - we assume that the robot has not started moving and that orientation values are set to the current absolute orientation
            //so first set of imu readings are effectively offsets
            offsetHeading = wrapAngleMinus(360 - imuAngles.firstAngle, turretHeading);
            offsetRoll = wrapAngleMinus(imuAngles.secondAngle, turretRoll);
            offsetPitch = wrapAngleMinus(imuAngles.thirdAngle, turretPitch);
            initialized = true;
        }

        //update current heading before doing any other calculations
        turretHeading = wrapAngle((360-imuAngles.firstAngle), offsetHeading);
        this.baseHeading = baseHeading;

//        double degreesOfSeparationForBase = diffAngle2(turretHeading, baseHeading);
//        double degreesOfSeparationForTarget = diffAngle2(turretHeading, turretTargetHeading);
//        double dangerLeft = wrapAngleMinus(baseHeading,Constants.DANGER_ZONE_WIDTH/2);
//        double dangerRight = (baseHeading + Constants.DANGER_ZONE_WIDTH/2) % 360;
//        double theMrVsSpecialVariable = Math.min(diffAngle2(turretHeading,dangerLeft), diffAngle2(turretHeading,dangerRight));
//        double directionOfTurn = degreesOfSeparationForTarget - 180;
//        double directionOfDanger = degreesOfSeparationForBase - 180;
//
//        if(between360(turretTargetHeading, dangerLeft, dangerRight)){
//            if(directionOfDanger > 0){
//                turretTargetHeading = (dangerLeft - Constants.DANGER_ZONE_WIDTH/4) % 360;
//            }
//            else{
//                turretTargetHeading = (dangerRight + Constants.DANGER_ZONE_WIDTH/4) % 360;
//            }
//        }
//
//        if(between360(dangerLeft, turretHeading, turretTargetHeading) && between360(dangerRight, turretHeading, turretTargetHeading)){
//            if(directionOfTurn > 0){
//                turretTargetHeading = (turretHeading + 20) % 360;
//            }
//            else{
//                turretTargetHeading = (turretHeading - 20) % 360;
//            }
//        }


        if(active) {
            maintainHeadingTurret();
        }
        else
            motor.setPower(0);
    }

    double turnIncrement = 20;
    public boolean setTurretHeadingDirectional(double finalHeading, Constants.DirectionOfTurn DoT){

            switch (DoT) {
                case LEFT:
                    if(diffAngle2(finalHeading, turretHeading) < 90 ){
                        turretTargetHeading = finalHeading;
                        return true;
                    }
                    turretTargetHeading = wrap360(turretHeading - turnIncrement);
                    break;
                case RIGHT:
                    if(diffAngle2(finalHeading, turretHeading) > 270 ){
                        turretTargetHeading = finalHeading;
                        return true;
                    }
                    turretTargetHeading = wrap360(turretHeading + turnIncrement);
                    break;
                case I_REALLY_DONT_CARE:
                    turretTargetHeading = finalHeading;
                    return true;
            }

        return false;
    }

    /**
     * assign the current heading of the robot to a specific angle
     * @param angle the value that the current heading will be assigned to
     */
    public void setHeading(double angle){
        turretHeading = angle;
        initialized = false; //triggers recalc of heading offset at next IMU update cycle
    }

    //public boolean getMagSensorVal() {return magSensor.getState(); }

    public boolean isActive(){
        return active;
    }

    public void setActive(boolean active){this.active = active;}

    public void adjust(double speed) {
        setTurntableAngle(getHeading(), 7.0 * speed);
    }

    public void rotateRight(double speed) {
        setTurntableAngle(getHeading(), angleIncrement * speed);
    }

    public void rotateLeft(double speed){

        setTurntableAngle(getHeading(), angleIncrement * -speed);

    }


    public boolean rotateCardinalTurret(boolean right){

        setTurntableAngle(nextCardinal(getHeading(),right,10));

        return true;
    }

    public void stopAll() {
        setPower(0);
        active = false;
    }

    public void setTurntableAngle(double currentAngle, double adjustAngle){
        turretTargetHeading=wrap360(currentAngle, adjustAngle);
    }

    public boolean setTurntableAngle(double angle){
        turretTargetHeading=wrap360(angle);
        return Conversions.between(getHeading(), angle - Constants.TURRET_TOLERANCE, angle + Constants.TURRET_TOLERANCE);
    }

    public void setPower(double pwr){
        motorPwr = pwr;
        motor.setPower(pwr);
    }

    double turnError = 0;
    public void movePIDTurret(double Kp, double Ki, double Kd, double currentAngle, double targetAngle) {

        //initialization of the PID calculator's output range, target value and multipliers
        turretPID.setOutputRange(-1, 1);
        turretPID.setPID(Kp, Ki, Kd);
        turretPID.setSetpoint(targetAngle);
        turretPID.enable();

        //initialization of the PID calculator's input range and current value
        turretPID.setInputRange(0, 360);
        turretPID.setContinuous();
        turretPID.setInput(currentAngle);

        turnError = diffAngle2(targetAngle, currentAngle);

        //calculates the angular correction to apply
        correction = turretPID.performPID();

        //performs the turn with the correction applied
        setPower(correction);
    }



    public void setTurretMotorMode(boolean IMUMODE){
        if(IMUMODE) {motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);}
        else{motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);}

    }

    public void maintainHeadingTurret(){
            //if this is the first time the button has been down, then save the heading that the robot will hold at and set a variable to tell that the heading has been saved
            if (!maintainHeadingInit) {
                motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
               //turretTargetHeading = turretHeading;
                maintainHeadingInit = true;
            }

            //hold the saved heading with PID
            switch (currentMode){
                case normalMode:
                    movePIDTurret(kpTurret,kiTurret,kdTurret,turretHeading,turretTargetHeading);
                    break;
                case baseBound:
                    movePIDTurret(kpTurret,kiTurret,kdTurret,turretHeading,baseHeading);
                    break;
            }
        }


    public double getHeading(){
        return turretHeading;
    }
    public double getTurretTargetHeading(){
        return turretTargetHeading;
    }
    public double getCorrection(){return correction;}
    public double getMotorPwr(){return motorPwr;}
    public double getMotorPwrActual(){return motor.getPower();}

    public TurretMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(TurretMode mode) {
        this.currentMode = mode;
        turretTargetHeading = wrap360(turretTargetHeading + Conversions.diffAngle2(baseHeading,turretTargetHeading));
    }

    private TurretMode currentMode = TurretMode.normalMode;
    public enum TurretMode {
        normalMode, // uses the turrets IMU in some way
        baseBound
    }
}
