package org.firstinspires.ftc.team16912.autonomous;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;

import java.net.PortUnreachableException;

public class PoseStorage {

    // Positions as Pose2d
    public static Pose2d RedLeft, RedRight, BlueLeft, BlueRight;
    public static Pose2d RedCarousel, BlueCarousel, RedHub, BlueHub, RedStorageUnit, BlueStorageUnit, RedWarehouse, BlueWarehouse, RedWarehouseSetup, BlueWarehouseSetup;

    public static Pose2d DetStartPose(int allI, int sideI)
    {
        // Red
        if(allI == 0)
        {
            //Left
            if(sideI == 0)
            {
                return RedLeft;
            }
            return RedRight;
        }
        // Blue
        if(sideI == 0)
        {
            return BlueLeft;
        }
        return BlueRight;
    }


    public static void initPoses() {


        // Start poses
        RedLeft = new Pose2d(-36.04860248750233, -64.287627195530014, Math.toRadians(90));
        RedRight = new Pose2d(12.04860248750233, -64.287627195530014, Math.toRadians(90));
        BlueLeft = new Pose2d(13.04860248750233, 64.287627195530014, Math.toRadians(270));
        BlueRight = new Pose2d(-35.04860248750233, 64.287627195530014, Math.toRadians(270));


        // Component poses
        RedCarousel = new Pose2d(-61.32295700521856,-62.766810012697775,Math.toRadians(269.28168016088995));
        BlueCarousel = new Pose2d(-59.43131663678813, 56.86792328414704,Math.toRadians(172.84226364522823));
        RedHub = new Pose2d( -14.000456450593105,-60.08996787199466,Math.toRadians(271.0642161002907));
        BlueHub = new Pose2d( -10.638173890493668, 60.95355503292278, Math.toRadians(90));
        RedStorageUnit = new Pose2d(-68.66711740422015, -40.326774389878032, Math.toRadians(0));
        BlueStorageUnit = new Pose2d(-58.664135706521755, 37.818617399773153, Math.toRadians(5));

        // Warehouse poses
        RedWarehouseSetup = new Pose2d(0, -70, Math.toRadians(90));
        RedWarehouse = new Pose2d(50.66711740422015, -40.326774389878032, Math.toRadians(0));
        BlueWarehouseSetup = new Pose2d(13, 67, Math.toRadians(270));
        BlueWarehouse = new Pose2d(45.664135706521755, 37.818617399773153, Math.toRadians(5));
    }

}
