package com.amrsatrio.server.util;

import net.minecraft.server.v1_14_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_14_R1.RayTrace;
import net.minecraft.server.v1_14_R1.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class RayTracer {
	private static final double DEFAULT_DISTANCE = 200;

	public static MovingObjectPositionBlock rayTrace(Entity entity) {
		return rayTrace(entity, DEFAULT_DISTANCE);
	}

	public static MovingObjectPositionBlock rayTrace(Entity entity, double distance) {
		Location loc = entity instanceof LivingEntity ? ((LivingEntity) entity).getEyeLocation() : entity.getLocation();
		return ((CraftWorld) loc.getWorld()).getHandle().rayTrace(new RayTrace(vectorToVec3D(loc.toVector()), vectorToVec3D(loc.toVector().clone().add(loc.toVector().add(loc.getDirection().multiply(distance)).multiply(distance))), RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, ((CraftEntity) entity).getHandle()));
	}

	private static Vec3D vectorToVec3D(Vector vector) {
		return new Vec3D(vector.getX(), vector.getY(), vector.getZ());
	}
}
