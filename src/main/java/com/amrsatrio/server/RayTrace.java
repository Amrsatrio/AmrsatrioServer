package com.amrsatrio.server;

import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.MovingObjectPosition;
import net.minecraft.server.v1_11_R1.Vec3D;
import net.minecraft.server.v1_11_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class RayTrace {
	public static final double a = 200;
	private boolean b = false;
	private World c;
	private BlockVector d;
	private BlockFace e;
	private Vector f;
	private BlockPosition g;

	protected RayTrace(World world, MovingObjectPosition movingobjectposition) {
		this.b = movingobjectposition != null;
		this.c = world;

		if (b) {
			this.d = new BlockVector(movingobjectposition.a().getX(), movingobjectposition.a().getY(), movingobjectposition.a().getZ());
			this.e = CraftBlock.notchToBlockFace(movingobjectposition.direction);
			this.f = new Vector(movingobjectposition.pos.x, movingobjectposition.pos.y, movingobjectposition.pos.z);
			this.g = movingobjectposition.a();
		}
	}

	public static RayTrace a(LivingEntity livingentity) {
		return a(livingentity, a);
	}

	public static RayTrace a(LivingEntity livingentity, double d0) {
		Location loc = livingentity.getEyeLocation();
		World world = loc.getWorld();
		return new RayTrace(world, a(world).rayTrace(a(loc.toVector()), a(loc.toVector().add(loc.getDirection().multiply(d0))), false));
	}

	private static Vec3D a(Vector vector) {
		return new Vec3D(vector.getX(), vector.getY(), vector.getZ());
	}

	private static WorldServer a(World world) {
		if (world instanceof CraftWorld) {
			return ((CraftWorld) world).getHandle();
		}
		throw new IllegalArgumentException("Cannot raytrace in a non CraftBukkit world!");
	}

	public static RayTrace a(World world, Vector vector, Vector vector1) {
		return a(world, vector, vector1, a);
	}

	public static RayTrace a(World world, Vector vector, Vector vector1, double d0) {
		return new RayTrace(world, a(world).rayTrace(a(vector), a(vector.clone().add(vector1.multiply(d0))), false));
	}

	public boolean a() {
		return b;
	}

	public BlockVector b() {
		return d;
	}

	public org.bukkit.block.Block c() {
		return b ? d.toLocation(c).getBlock() : null;
	}

	public BlockFace d() {
		return e;
	}

	public Vector e() {
		return f;
	}

	public BlockPosition f() {
		return g;
	}
}
