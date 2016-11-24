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

	public static RayTrace a(LivingEntity entity) {
		return a(entity, a);
	}

	public static RayTrace a(LivingEntity entity, double length) {
		Location loc = entity.getEyeLocation();
		World world = loc.getWorld();
		return new RayTrace(world, a(world).rayTrace(a(loc.toVector()),
				a(loc.toVector().add(loc.getDirection().multiply(length))), false));
	}

	private static Vec3D a(Vector vec) {
		return new Vec3D(vec.getX(), vec.getY(), vec.getZ());
	}

	private static WorldServer a(World world) {
		if (world instanceof CraftWorld) return ((CraftWorld) world).getHandle();
		throw new IllegalArgumentException("Cannot raytrace in a non CraftBukkit world!");
	}

	public static RayTrace a(World world, Vector start, Vector direction) {
		return a(world, start, direction, a);
	}

	public static RayTrace a(World world, Vector start, Vector direction, double length) {
		return new RayTrace(world,
				a(world).rayTrace(a(start), a(start.clone().add(direction.multiply(length))), false));
	}

	private boolean b = false;
	private World c;
	private BlockVector d;
	private BlockFace e;
	private Vector f;
	private BlockPosition g;

	protected RayTrace(World p0, MovingObjectPosition p1) {
		this.b = p1 != null;
		this.c = p0;
		if (b) {
			this.d = new BlockVector(p1.a().getX(), p1.a().getY(), p1.a().getZ());
			this.e = CraftBlock.notchToBlockFace(p1.direction);
			this.f = new Vector(p1.pos.x, p1.pos.y, p1.pos.z);
			this.g = p1.a();
		}
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
