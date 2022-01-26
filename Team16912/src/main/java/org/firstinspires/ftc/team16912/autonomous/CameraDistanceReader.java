package org.firstinspires.ftc.team16912.autonomous;

import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.team16912.drive.SampleMecanumDrive;
import org.firstinspires.ftc.team16912.util.LinguineHardware;
import org.opencv.core.Rect;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvWebcam;

@Autonomous(name = "Camera Distance Reader")
public class CameraDistanceReader extends LinearOpMode {

    int cameraGroundHeight = 65; // Height of the center of the lens from the ground
    int blockHeight = 50; // Height of the freight block

    private int distance;

    DistancePipeline pipeline;
    OpenCvInternalCamera webcam;

    LinguineHardware robot = new LinguineHardware();
    SampleMecanumDrive drive;

    public void runOpMode() {


        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        pipeline = new DistancePipeline();
        webcam.setPipeline(pipeline);

        webcam.setViewportRenderingPolicy(OpenCvCamera.ViewportRenderingPolicy.OPTIMIZE_VIEW);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.SIDEWAYS_LEFT);
            }

            @Override
            public void onError(int errorCode) {
                /*
                 * This will be called if the camera could not be opened
                 */
            }
        });

        robot.init(hardwareMap);
        drive = new SampleMecanumDrive(hardwareMap);


        waitForStart();

        if(isStarted())
        {

            centerRobot();

            Trajectory runToBlock = drive.trajectoryBuilder(drive.getPoseEstimate())
                    .forward(pipeline.DISTANCE - 5)
                    .build();

            openClaw();
            drive.followTrajectory(runToBlock);
            runArmTo(250);
            closeClaw();
            runArmTo(-1000);
        }

    }

    private void centerRobot()
    {
        Rect block = pipeline.BLOCK;
        int blockCenter = block.x + (block.width/2);
        double pxpi = block.height/2.0;
        double distToEdge = 0.0;
        double strafeDistance = 0.0;

        Trajectory alignBlock;

        // Block is to the left
        if(blockCenter < 160)
        {
            distToEdge = block.x;
            strafeDistance = distToEdge/pxpi;

            /*alignBlock = drive.trajectoryBuilder(drive.getPoseEstimate())
                    .strafeLeft((strafeDistance)+1)
                    .build();*/
        }

        // Block is to the right
        else if (blockCenter > 160) {
            distToEdge = 320 - (block.x + block.width);
            strafeDistance = (distToEdge) / pxpi;
            /*alignBlock = drive.trajectoryBuilder(drive.getPoseEstimate())
                    .strafeRight((strafeDistance)-1)
                    .build();*/
        }

        else {
            /*alignBlock = drive.trajectoryBuilder(drive.getPoseEstimate())
                    .forward(0)
                    .build();*/
        }

        telemetry.addData("Strafe Distance:", strafeDistance);
        telemetry.addData("Error:", getError(distToEdge, pipeline.DISTANCE));
        telemetry.addData("Distance: ", pipeline.DISTANCE);
        telemetry.addData("Distane to Edge: ", distToEdge);
        telemetry.update();
        //drive.followTrajectory(alignBlock);

    }

    private double getError(double distToEdge, double distToBlock)
    {
        double ycomp = 1.125 * Math.pow(1.0675, distToBlock);
        double xcomp;

        double xtop = (Math.pow(Math.abs(distToEdge - 160), 2));

        Rect block = pipeline.BLOCK;
        int blockCenter = block.x + (block.width/2);

        if (blockCenter < 160) {
            xcomp = xtop / 25000;
        }

        else if (blockCenter > 160) {
            xcomp = xtop / 17750;
        }

        else xcomp = 0;

        return ycomp * xcomp;
    }

    // Closes claw
    private void closeClaw() { robot.servoClaw.setPosition(.7); }

    // Opens claw
    private void openClaw() { robot.servoClaw.setPosition(-1); }

    // Return arm to start
    private void runArmTo(int encoderPos) {
        while (robot.armEncoder.getCurrentPosition() < encoderPos) {
            for (DcMotorEx motor : robot.motorArms) {
                motor.setPower(.5);
            }
        }
        for (DcMotorEx motor : robot.motorArms) motor.setPower(0);
    }


}
