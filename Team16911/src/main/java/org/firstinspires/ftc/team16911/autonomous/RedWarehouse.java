package org.firstinspires.ftc.team16911.autonomous;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.team16911.drive.SampleMecanumDrive;
import org.firstinspires.ftc.team16911.hardware.RigatoniHardware;

@Autonomous(name = "RedWarehouse")
public class RedWarehouse extends LinearOpMode
{
    private SampleMecanumDrive drive;

    private int initialWaitTime = 0;
    private int maxAttempts = 2;
    private double blockPickupPositionY = -41.5;
    private boolean goForNewBlock = true;

    private final Pose2d barcode = new Pose2d(20, .5, 0);
    private final Pose2d hubLevelOnePose = new Pose2d(16.8, 19.25, 0);
    private final Pose2d hubLevelTwoPose = new Pose2d(17, 19.25, 0);
    private final Pose2d hubLevelThreePose = new Pose2d(23, 19.25, 0);
    private final Pose2d warehouseOutside = new Pose2d(-.25, -20, 0);
    private final Pose2d warehouse = new Pose2d(-.25, -32, 0);

    private Trajectory toBarcode;
    private Utilities utilities;
    private final Trajectory[] toHubTrajectories = new Trajectory[3];
    private final Trajectory[] fromHubTrajectories = new Trajectory[3];


    public void runOpMode()
    {
        // Configuration
        final int startPosition = 60;
        RigatoniHardware hardware = new RigatoniHardware();
        hardware.init(hardwareMap);
        utilities = new Utilities(hardware);

        // Initialize Mecanum Drive
        drive = new SampleMecanumDrive(hardwareMap);
        drive.setPoseEstimate(new Pose2d());
        utilities.moveArm(startPosition);
        buildTrajectories();

        configuration();

        waitForStart();
        if (!opModeIsActive())
        {
            return;
        }

        ElapsedTime autonomousTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        utilities.wait(initialWaitTime, telemetry);
        utilities.moveArm(utilities.positions[1]);

        drive.followTrajectory(toBarcode);
        int barcodeLevel = utilities.getBarcodeLevelRedSide();
        utilities.moveArm(utilities.positions[barcodeLevel]);
        telemetry.addData("Right Distance", hardware.rightDistanceSensor.getDistance(DistanceUnit.INCH));
        telemetry.addData("left Distance", hardware.leftDistanceSensor.getDistance(DistanceUnit.INCH));
        telemetry.update();

        drive.followTrajectory(toHubTrajectories[barcodeLevel]);
        utilities.eliminateOscillations();
        utilities.dropCargo(utilities.CARGO_DROP_TIME, telemetry);

        drive.followTrajectory(fromHubTrajectories[barcodeLevel]);

        int attempts = 0;
        while (goForNewBlock && 30000 - autonomousTime.time() > 10000 && attempts < maxAttempts)
        {
            pickupNewBlock(hardware);
            blockPickupPositionY -= 2;
            attempts++;
        }
    }

    private void buildTrajectories()
    {
        // Configuration Variables
        Trajectory toHubLevelTwo, toHubLevelThree, fromHubLevelOne, fromHubLevelTwo;
        Trajectory fromHubLevelThree, toHubLevelOne;

        toBarcode = drive.trajectoryBuilder(drive.getPoseEstimate())
                .lineToLinearHeading(barcode).build();

        toHubLevelOne = drive.trajectoryBuilder(toBarcode.end())
                .lineToLinearHeading(hubLevelOnePose).build();

        toHubLevelTwo = drive.trajectoryBuilder(toBarcode.end())
                .lineToLinearHeading(hubLevelTwoPose).build();

        toHubLevelThree = drive.trajectoryBuilder(toBarcode.end())
                .lineToLinearHeading(hubLevelThreePose).build();

        fromHubLevelOne = drive.trajectoryBuilder(toHubLevelOne.end(), -Math.toRadians(135))
                .splineToLinearHeading(warehouseOutside, -Math.toRadians(90))
                .splineToLinearHeading(warehouse, -Math.toRadians(90)).build();

        fromHubLevelTwo = drive.trajectoryBuilder(toHubLevelTwo.end(), -Math.toRadians(140))
                .splineToLinearHeading(warehouseOutside, -Math.toRadians(90))
                .splineToLinearHeading(warehouse, -Math.toRadians(90)).build();

        fromHubLevelThree = drive.trajectoryBuilder(toHubLevelThree.end(), -Math.toRadians(140))
                .splineToLinearHeading(warehouseOutside, -Math.toRadians(90))
                .splineToLinearHeading(warehouse, -Math.toRadians(90)).build();

        toHubTrajectories[0] = toHubLevelOne;
        toHubTrajectories[1] = toHubLevelTwo;
        toHubTrajectories[2] = toHubLevelThree;

        fromHubTrajectories[0] = fromHubLevelOne;
        fromHubTrajectories[1] = fromHubLevelTwo;
        fromHubTrajectories[2] = fromHubLevelThree;
    }

    private void pickupNewBlock(RigatoniHardware hardware)
    {
        final double blockPickupPositionX = 7;
        final double returnSetupPositionX = 4;

        final Pose2d blockPickupOne = new Pose2d(7, -37, Math.toRadians(-90));
        final Pose2d blockPickupTwo = new Pose2d(blockPickupPositionX, blockPickupPositionY, Math.toRadians(-90));
        final Pose2d returnTrajectorySetup = new Pose2d(returnSetupPositionX, -33, 0);
        final Pose2d toHub = new Pose2d(23, 21.75, 0);
        final Vector2d toHubReturn = new Vector2d(-.25, 10);
        final Vector2d warehouse = new Vector2d(-.25, -32);

        drive.update();

        utilities.moveArm(10);
        utilities.intakeCargo();

        Trajectory blockPickupTrajectory = drive.trajectoryBuilder(drive.getPoseEstimate(), Math.toRadians(0))
                .splineToSplineHeading(blockPickupOne, Math.toRadians(-90))
                .splineToSplineHeading(blockPickupTwo, Math.toRadians(-90)).build();

        drive.followTrajectory(blockPickupTrajectory);
        drive.update();

        utilities.stopIntake();
        utilities.moveArm(utilities.positions[2]);

        double distanceFromWall = hardware.rightDistanceSensor.getDistance(DistanceUnit.INCH);
        double yPose = -distanceFromWall + 57;
        drive.setPoseEstimate(new Pose2d(blockPickupPositionX, -yPose, Math.toRadians(-90)));
        drive.update();

        Trajectory toHubTrajectory = drive.trajectoryBuilder(drive.getPoseEstimate(), Math.toRadians(90))
                .splineToSplineHeading(returnTrajectorySetup, Math.toRadians(140))
                .splineToLinearHeading(this.warehouse, Math.toRadians(90))
                .splineToLinearHeading(new Pose2d(), Math.toRadians(90))
                .splineToLinearHeading(toHub, Math.toRadians(45)).build();

        drive.followTrajectory(toHubTrajectory);
        drive.update();

        double backDistance = hardware.backDistanceSensor.getDistance(DistanceUnit.INCH);
        backDistance = backDistance - 2.5;
        drive.setPoseEstimate(new Pose2d(backDistance, 21.75, 0));

        utilities.dropCargo(utilities.CARGO_DROP_TIME, telemetry);

        Trajectory returnToWarehouse = drive.trajectoryBuilder(drive.getPoseEstimate(), Math.toRadians(-150))
                .splineToConstantHeading(toHubReturn, Math.toRadians(-90))
                .splineToConstantHeading(warehouse, Math.toRadians(-90)).build();

        drive.followTrajectory(returnToWarehouse);
    }

    private void configuration()
    {
        ElapsedTime buttonTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
        int lockoutTime = 200;

        while (!isStarted() && !gamepad1.cross)
        {
            if (gamepad1.dpad_up && buttonTime.time() > lockoutTime)
            {
                initialWaitTime = Math.min(30000, initialWaitTime + 1000);
                buttonTime.reset();
            }
            else if (gamepad1.dpad_down && buttonTime.time() > lockoutTime)
            {
                initialWaitTime = Math.max(0, initialWaitTime - 1000);
                buttonTime.reset();
            }
            else if (gamepad1.circle)
            {
                initialWaitTime = 0;
            }
            else if (gamepad1.triangle && buttonTime.time() > lockoutTime)
            {
                goForNewBlock = !goForNewBlock;
                buttonTime.reset();
            }
            else if (gamepad1.dpad_left)
            {
                maxAttempts = Math.max(0, maxAttempts - 1);
            }
            else if (gamepad1.dpad_right)
            {
                maxAttempts = Math.max(2, maxAttempts + 1);
            }

            telemetry.addData("Initial Wait Time", initialWaitTime / 1000);
            telemetry.addData("Go For New BLock", goForNewBlock);
            if (goForNewBlock) {telemetry.addData("Max Attempts", maxAttempts);}
            telemetry.update();
        }

        telemetry.addData("Status", "Confirmed");
        telemetry.update();
    }
}
